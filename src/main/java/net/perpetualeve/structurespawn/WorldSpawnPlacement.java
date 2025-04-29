package net.perpetualeve.structurespawn;

import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.server.ServerLifecycleHooks;

public class WorldSpawnPlacement extends StructurePlacement {

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
			    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("max_distance_from_world_spawn", 100)
				    .forGetter(WorldSpawnPlacement::maxDistanceFromWorldOrigin))
		    .apply(instance, instance.stable(WorldSpawnPlacement::new)));
    private final int distance;
    private static MutableBoolean generated = new MutableBoolean(false);

    @SuppressWarnings("deprecation")
    protected WorldSpawnPlacement(Vec3i pLocateOffset, FrequencyReductionMethod pFrequencyReductionMethod,
	    float pFrequency, int pSalt, Optional<ExclusionZone> pExclusionZone, int distance) {
	super(pLocateOffset, pFrequencyReductionMethod, pFrequency, pSalt, pExclusionZone);
	this.distance = distance;
	generated.setFalse();
    }

    public int maxDistanceFromWorldOrigin() {
	return this.distance;
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState pStructureState, int pX, int pZ) {
	MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
	ServerLevelData serverData = server.getWorldData().overworldData();
	int x = serverData.getXSpawn();
	int z = serverData.getZSpawn();

	long xBlockPos = pX * 16L;
	long zBlockPos = pZ * 16L;

	long dX = x - xBlockPos;
	long dZ = z - zBlockPos;

	/**
	 * We want it to be within the distance so we cancel when it isnt
	 */
	if ((dX * dX + dZ * dZ) > (maxDistanceFromWorldOrigin() * maxDistanceFromWorldOrigin())) {
	    return false;
	}
	
	return true;
    }

    @Override
    public boolean isStructureChunk(ChunkGeneratorStructureState pStructureState, int pX, int pZ) {
	boolean res = super.isStructureChunk(pStructureState, pX, pZ);
	if (res  && generated.isFalse()) {
	    StructureSpawn.LOGGER.info("""
	    	Worldspawn Structure was attempted at Chunk
	    		X: %d
	    		Z: %d
	    	""".formatted(pX * 16L, pZ * 16L));
	    generated.setTrue();
	    return true;
	}
	return false;
    }

    @Override
    public StructurePlacementType<?> type() {
	return StructurePlacements.WORLDSPAWN_PLACEMENT.get();
    }

}
