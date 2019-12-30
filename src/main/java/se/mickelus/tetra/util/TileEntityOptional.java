package se.mickelus.tetra.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Optional;

public class TileEntityOptional {
    public static <T> Optional<T> from(IBlockReader world, BlockPos pos, Class<T> tileEntityClass) {
        return CastOptional.cast(world.getTileEntity(pos), tileEntityClass);
    }
}
