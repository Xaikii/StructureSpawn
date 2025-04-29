package net.perpetualeve.structurespawn;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public class IntervalPlacement extends StructurePlacement {

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
			    Codec.BOOL.optionalFieldOf("centerIncluded", false).forGetter(IntervalPlacement::center))
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
	    int pSalt, Optional<ExclusionZone> pExclusionZone, int interval, int variation, boolean centerIncluded) {
	super(pLocateOffset, pFrequencyReductionMethod, pFrequency, pSalt, pExclusionZone);
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
	long xPos = (pX * 16L);
	long zPos = (pZ * 16L);

	// Here we check specifically the center
	if (centerIncluded && withinReach(xPos) && withinReach(zPos))
	    return false;
	if (withinReach(xPos % interval) && withinReach(zPos % interval)) {
	    return true;
	}
	return false;
    }

    public boolean withinReach(long pos) {
	return (pos > interval - variation) || (pos < variation);
    }

    @Override
    public StructurePlacementType<?> type() {
	return StructurePlacements.INTERVAL_PLACEMENT.get();
    }

}
