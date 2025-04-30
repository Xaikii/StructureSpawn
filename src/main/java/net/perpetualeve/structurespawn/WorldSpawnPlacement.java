package net.perpetualeve.structurespawn;

import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.server.ServerLifecycleHooks;

public class WorldSpawnPlacement extends RandomSpreadStructurePlacement {

    @SuppressWarnings("deprecation")
    public static final Codec<WorldSpawnPlacement> CODEC = RecordCodecBuilder
	    .create((instance) -> instance
		    .group(Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO)
			    .forGetter(WorldSpawnPlacement::locateOffset),
			    StructurePlacement.FrequencyReductionMethod.CODEC
				    .optionalFieldOf("frequency_reduction_method",
					    StructurePlacement.FrequencyReductionMethod.DEFAULT)
				    .forGetter(WorldSpawnPlacement::frequencyReductionMethod),
			    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("frequency", 1.0F)
				    .forGetter(WorldSpawnPlacement::frequency),
			    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(WorldSpawnPlacement::salt),
			    StructurePlacement.ExclusionZone.CODEC.optionalFieldOf("exclusion_zone")
				    .forGetter(WorldSpawnPlacement::exclusionZone),
			    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("spacing", 1000)
				    .forGetter(WorldSpawnPlacement::spacing),
			    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("separation", 900)
				    .forGetter(WorldSpawnPlacement::separation),
			    RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR)
				    .forGetter(WorldSpawnPlacement::spreadType),
			    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("max_distance_from_world_spawn", 100)
				    .forGetter(WorldSpawnPlacement::maxDistanceFromWorldOrigin))
		    .apply(instance, instance.stable(WorldSpawnPlacement::new)));
    private final int distance;
    private static MutableBoolean generated = new MutableBoolean(false);

    @SuppressWarnings("deprecation")
    protected WorldSpawnPlacement(Vec3i pLocateOffset, FrequencyReductionMethod pFrequencyReductionMethod,
	    float pFrequency, int pSalt, Optional<ExclusionZone> pExclusionZone, int spacing, int separation,
	    RandomSpreadType spreadType, int distance) {
	super(pLocateOffset, pFrequencyReductionMethod, pFrequency, pSalt, pExclusionZone, 1000, 900, spreadType);
	this.distance = distance;
	generated.setFalse();
    }

    public int maxDistanceFromWorldOrigin() {
	return this.distance;
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState pStructureState, int pX, int pZ) {
	ChunkPos pos = getPotentialStructureChunk(salt(), pX, pZ);
	return pos.x == pX && pos.z == pZ;
    }

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState pStructureState, int pX, int pZ) {
	boolean res = super.isStructureChunk(pStructureState, pX, pZ);
	if (res) {
	    StructureSpawn.LOGGER.info("""
	    	Worldspawn Structure was attempted at Chunk
	    		X: %d
	    		Z: %d
	    	""".formatted(pX * 16L, pZ * 16L));
	    return true;
	}
	return false;
    }

    @Override
    public ChunkPos getPotentialStructureChunk(long pSeed, int pRegionX, int pRegionZ) {
	ServerLevelData serverData = ServerLifecycleHooks.getCurrentServer().getWorldData().overworldData();
	int x1, x = serverData.getXSpawn();
	int z1, z = serverData.getZSpawn();
	WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
	worldgenrandom.setLargeFeatureWithSalt(pSeed, pRegionX, pRegionZ, this.salt());
	x1 = spreadType().evaluate(worldgenrandom, distance);
	z1 = spreadType().evaluate(worldgenrandom, distance);
	return new ChunkPos(Math.floorDiv(x + x1, 16), Math.floorDiv(z + z1, 16));
    }

    @Override
    public StructurePlacementType<?> type() {
	return StructurePlacements.WORLDSPAWN_PLACEMENT.get();
    }

}
