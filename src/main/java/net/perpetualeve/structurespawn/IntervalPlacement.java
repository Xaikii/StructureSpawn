package net.perpetualeve.structurespawn;

import java.util.Optional;

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

public class IntervalPlacement extends RandomSpreadStructurePlacement {

    @SuppressWarnings("deprecation")
    public static final Codec<IntervalPlacement> CODEC = RecordCodecBuilder
	    .create((instance) -> instance
		    .group(Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO)
			    .forGetter(IntervalPlacement::locateOffset),
			    StructurePlacement.FrequencyReductionMethod.CODEC
				    .optionalFieldOf("frequency_reduction_method",
					    StructurePlacement.FrequencyReductionMethod.DEFAULT)
				    .forGetter(IntervalPlacement::frequencyReductionMethod),
			    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("frequency", 1.0F)
				    .forGetter(IntervalPlacement::frequency),
			    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(IntervalPlacement::salt),
			    StructurePlacement.ExclusionZone.CODEC.optionalFieldOf("exclusion_zone")
				    .forGetter(IntervalPlacement::exclusionZone),
			    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("interval").forGetter(IntervalPlacement::getInterval),
			    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("variation", 32)
				    .forGetter(IntervalPlacement::getVariation),
			    Codec.BOOL.optionalFieldOf("centerIncluded", false).forGetter(IntervalPlacement::center),
			    RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR)
				    .forGetter(IntervalPlacement::spreadType))
		    .apply(instance, instance.stable(IntervalPlacement::new)));
    /**
     * The distance between attempts
     */
    private final int interval;
    /**
     * The amount of blocks in either directions it will continue trying
     */
    private final int variation;
    private final boolean centerIncluded;

    @SuppressWarnings("deprecation")
    public IntervalPlacement(Vec3i pLocateOffset, FrequencyReductionMethod pFrequencyReductionMethod, float pFrequency,
	    int pSalt, Optional<ExclusionZone> pExclusionZone, int interval, int variation, boolean centerIncluded,
	    RandomSpreadType spreadType) {
	super(pLocateOffset, pFrequencyReductionMethod, pFrequency, pSalt, pExclusionZone, interval, variation,
		spreadType);
	this.interval = 500;
	this.variation = variation;
	this.centerIncluded = centerIncluded;
    }

    public int getInterval() {
	return interval;
    }

    public int getVariation() {
	return variation;
    }

    public boolean center() {
	return centerIncluded;
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState pStructureState, int pX, int pZ) {

	int n = Math.floorDiv(interval, 16);
	if (!centerIncluded && (pX < n && pX > -n) && (pZ < n && pZ > -n)) {
	    return false;
	}

	ChunkPos pos = getPotentialStructureChunk(this.salt(), pX, pZ);
	return pos.x == pX && pos.z == pZ;
    }

    @Override
    public ChunkPos getPotentialStructureChunk(long pSeed, int pRegionX, int pRegionZ) {
	int i = Math.floorDiv(pRegionX * 16, this.spacing());
	int j = Math.floorDiv(pRegionZ * 16, this.spacing());
	WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
	worldgenrandom.setLargeFeatureWithSalt(pSeed, i, j, this.salt());
	int l = this.spreadType().evaluate(worldgenrandom, separation());
	int i1 = this.spreadType().evaluate(worldgenrandom, separation());
	return new ChunkPos(Math.floorDiv(i * this.spacing() + l, 16), Math.floorDiv(j * this.spacing() + i1, 16));
    }

    public boolean withinReach(long pos) {
	return (pos > interval - variation) || (pos < variation);
    }

    @Override
    public StructurePlacementType<?> type() {
	return StructurePlacements.INTERVAL_PLACEMENT.get();
    }

}
