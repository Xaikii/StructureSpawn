package net.perpetualeve.structurespawn;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.server.ServerLifecycleHooks;

public class WorldSpawnPlacement extends StructurePlacement {

	public static final Codec<WorldSpawnPlacement> CODEC = ExtraCodecs
			.validate(MapCodec.unit(WorldSpawnPlacement::new), WorldSpawnPlacement::validate).codec();

	private static DataResult<WorldSpawnPlacement> validate(WorldSpawnPlacement p_286361_) {
		return DataResult.success(p_286361_);
	}

	public WorldSpawnPlacement() {
		super(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0F, 1234, Optional.empty());
	}

	public static ChunkPos pos = null;

	@Override
	public boolean isStructureChunk(ChunkGeneratorStructureState pStructureState, int pX, int pZ) {
		return this.isPlacementChunk(pStructureState, pX, pZ);
	}

	@Override
	protected boolean isPlacementChunk(ChunkGeneratorStructureState pStructureState, int pX, int pZ) {
		if (pos == null) {
			ServerLevelData serverData = ServerLifecycleHooks.getCurrentServer().getWorldData().overworldData();
			pos = new ChunkPos(new BlockPos(serverData.getXSpawn(), 0, serverData.getZSpawn()));
		}
		return pos.x == pX && pos.z == pZ;
	}

	@Override
	public StructurePlacementType<?> type() {
		return StructurePlacements.WORLDSPAWN_PLACEMENT.get();
	}
}
