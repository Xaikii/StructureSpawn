package net.perpetualeve.structurespawn;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface StructurePlacements {

    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENTS = DeferredRegister
	    .create(Registries.STRUCTURE_PLACEMENT, StructureSpawn.MODID);
    RegistryObject<StructurePlacementType<WorldSpawnPlacement>> WORLDSPAWN_PLACEMENT = STRUCTURE_PLACEMENTS.register("worldspawn", () -> () -> WorldSpawnPlacement.CODEC);
    RegistryObject<StructurePlacementType<IntervalPlacement>> INTERVAL_PLACEMENT = STRUCTURE_PLACEMENTS.register("intervalspawn", () -> () -> IntervalPlacement.CODEC);
    RegistryObject<StructurePlacementType<CoordinatePlacement>> COORDINATE_PLACEMENT = STRUCTURE_PLACEMENTS.register("coordinate", () -> () -> CoordinatePlacement.CODEC);
}
