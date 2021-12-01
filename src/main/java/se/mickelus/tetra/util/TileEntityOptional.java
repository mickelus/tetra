package se.mickelus.tetra.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.World;

import java.util.Optional;

public class TileEntityOptional {
    public static <T> Optional<T> from(BlockGetter world, BlockPos pos, Class<T> tileEntityClass) {
        return CastOptional.cast(world.getBlockEntity(pos), tileEntityClass);
    }
}
