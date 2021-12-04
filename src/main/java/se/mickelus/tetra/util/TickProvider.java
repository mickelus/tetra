package se.mickelus.tetra.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.BiFunction;

@ParametersAreNonnullByDefault
public class TickProvider<T extends BlockEntity & ITetraTicker> implements BlockEntityTicker<T> {
	private final BlockEntityType<T> entityType;
	private final BiFunction<BlockPos, BlockState, T> create;

	public TickProvider(BlockEntityType<T> entityType, BiFunction<BlockPos, BlockState, T> create) {
		this.entityType = entityType;
		this.create = create;
	}

	@Override
	public void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
		blockEntity.tick(level, pos, state);
	}

	public T create(BlockPos pos, BlockState state) {
		return create.apply(pos, state);
	}

	public <V extends BlockEntity> Optional<BlockEntityTicker<V>> forTileType(BlockEntityType<V> type) {
		if (type.equals(entityType))
			return Optional.of((BlockEntityTicker<V>) this);
		return Optional.empty();
	}
}
