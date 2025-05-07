package net.perpetualeve.structurespawn;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface StructureTypes {

	public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister
			.create(Registries.STRUCTURE_TYPE, StructureSpawn.MODID);
	RegistryObject<StructureType<CaveStructure>> CAVE_STRUCTURE = STRUCTURE_TYPES.register("cave_structure", () -> () -> CaveStructure.CODEC);
}
