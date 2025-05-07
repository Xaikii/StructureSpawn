package net.perpetualeve.structurespawn;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.ConfigType;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigEntry.ParsedArray;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.config.ConfigSettings;
import carbonconfiglib.config.MappedConfig;
import carbonconfiglib.utils.AutomationType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.level.LevelEvent.CreateSpawnPosition;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(StructureSpawn.MODID)
public class StructureSpawn {
    public static final String MODID = "structurespawn";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ConfigHandler CONFIG;
    public static ConfigSection VALUES;
    public static MappedConfig<ResourceLocation, Double> BIOME_WEIGHT;
    public static ParsedArray<BiomeWeight> WEIGHT;

    public static final Executor POOL = Executors.newCachedThreadPool();

    public StructureSpawn() {

	Config config = new Config("structurespawn");

	CONFIG = CarbonConfig.CONFIGS.createConfig(config,
		ConfigSettings.withConfigType(ConfigType.SERVER)
			.withAutomations(AutomationType.AUTO_RELOAD, AutomationType.AUTO_SYNC, AutomationType.AUTO_LOAD)
			.withBaseFolder(CarbonConfig.CONFIGS.getBasePath().resolve("perpetualMods")));

	VALUES = new ConfigSection("Spawn Biomes");

	WEIGHT = VALUES.addParsedArray("Biome Weight", BiomeWeight.createDefault(), BiomeWeight.createParser(),
		"The weight for each Biome to be eligable to be the chosen for Spawns. Suggestions only exist while in a World.");
	BIOME_WEIGHT = MappedConfig.create(CONFIG, WEIGHT, (BiomeWeight obj) -> {
	    return obj.biome;
	}, (BiomeWeight obj) -> {
	    return obj.weight;
	});

	config.add(VALUES);

	CONFIG.register();

	IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
	MinecraftForge.EVENT_BUS.register(this);
	StructurePlacements.STRUCTURE_PLACEMENTS.register(modEventBus);
	StructureTypes.STRUCTURE_TYPES.register(modEventBus);

    }
    
    @SubscribeEvent
    public void serverStarted(ServerStartedEvent event) {
	WEIGHT.clearSuggestions();
	for (Entry<ResourceKey<Biome>, Biome> biome : ForgeRegistries.BIOMES.getEntries()) {
	    WEIGHT.addSuggestion(new BiomeWeight(biome.getKey().location(), 100));
	}
	CONFIG.save();
    }

    @SubscribeEvent
    public void spawnPosition(CreateSpawnPosition event) {
	LOGGER.info("Starting spawn relocation");
	long time = System.nanoTime();
	WeightedRandom<ResourceLocation> random = new WeightedRandom<>();
	Set<Entry<ResourceLocation, Double>> list = BIOME_WEIGHT.entrySet();
	if (list.size() < 1) {
	    LOGGER.info("Stopped spawn relocation as there are no Biome Entries");
	    return;
	}
	for (Entry<ResourceLocation, Double> entry : list) {
	    random.add(entry.getValue(), entry.getKey());
	}
	ResourceLocation result = random.next();

	ServerLevelData data = event.getSettings();
	BlockPos spawnPos = new BlockPos(data.getXSpawn(), data.getYSpawn(), data.getXSpawn());

	LevelAccessor level = event.getLevel();

	ServerLevel cache = ((ServerLevel) level);
	Pair<BlockPos, Holder<Biome>> pair = cache.findClosestBiome3d((biome) -> {
	    return biome.is(result);
	}, spawnPos, 50000, 32, 128);

	if (pair == null) {
	    LOGGER.info("Stopped spawn relocation as no Biomes were found");
	    return;
	}

	data.setSpawn(pair.getFirst(), data.getSpawnAngle());
	System.out.println("Position: " + pair.getFirst());
	event.setCanceled(true);

	LOGGER.info(toLagString("Finished spawn relocation in ", System.nanoTime() - time) + "! ");
    }

    public static String toLagString(String prefix, long lag) {
	return new StringBuilder(prefix).append(": ").append(lag / 1000000L).append("ms, ").append(lag / 1000L)
		.append("µs, ").append(lag).append("ns").toString();
    }

    @SubscribeEvent
    public void worldUnload(LevelEvent.Unload event) {
    	WorldSpawnPlacement.pos = null;
    }
}
