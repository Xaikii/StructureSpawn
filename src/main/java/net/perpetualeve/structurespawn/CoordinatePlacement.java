package net.perpetualeve.structurespawn;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.server.ServerLifecycleHooks;

public class CoordinatePlacement extends StructurePlacement {

	public static final Codec<CoordinatePlacement> CODEC = RecordCodecBuilder.create((instance) -> instance
			.group(Codec.INT.fieldOf("x").forGetter(place -> place.pos.getMinBlockX()),
					Codec.INT.fieldOf("z").forGetter(place -> place.pos.getMinBlockZ()),
					Codec.BOOL.optionalFieldOf("relative", false).forGetter(null))
			.apply(instance, instance.stable(CoordinatePlacement::new)));

	public final ChunkPos pos;
	public final boolean relative;

	public CoordinatePlacement(int x, int z, boolean relative) {
		super(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0F, 1234, Optional.empty());
		this.pos = new ChunkPos(new BlockPos(x, 0, z));
		this.relative = relative;
	}

	@Override
	public boolean isStructureChunk(ChunkGeneratorStructureState pStructureState, int pX, int pZ) {
		return this.isPlacementChunk(pStructureState, pX, pZ);
	}

	@Override
	protected boolean isPlacementChunk(ChunkGeneratorStructureState pStructureState, int pX, int pZ) {
		if (relative) {
			ServerLevelData serverData = ServerLifecycleHooks.getCurrentServer().getWorldData().overworldData();
			ChunkPos spawnPos = new ChunkPos(new BlockPos(serverData.getXSpawn(), 0, serverData.getZSpawn()));
			return (pos.x + spawnPos.x) == pX && (pos.z + spawnPos.z) == pZ;
		}
		return pos.x == pX && pos.z == pZ;
	}

	@Override
	public StructurePlacementType<?> type() {
		return StructurePlacements.COORDINATE_PLACEMENT.get();
	}
}
