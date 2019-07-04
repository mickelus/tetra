package se.mickelus.tetra.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.Chunk;

import java.util.Optional;

public class TileEntityOptional {
    public static <T> Optional<T> from(IBlockAccess world, BlockPos pos, Class<T> tileEntityClass) {
        TileEntity tileEntity;

        if (world instanceof ChunkCache) {
            tileEntity = ((ChunkCache)world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
        } else {
            tileEntity = world.getTileEntity(pos);
        }

        return CastOptional.cast(tileEntity, tileEntityClass);
    }
}
