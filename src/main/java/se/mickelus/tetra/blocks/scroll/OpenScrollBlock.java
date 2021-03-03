package se.mickelus.tetra.blocks.scroll;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.util.RotationHelper;

public class OpenScrollBlock extends ScrollBlock {

    public static final String identifier = "scroll_open";
    @ObjectHolder(TetraMod.MOD_ID + ":" + identifier)
    public static ScrollBlock instance;

    public OpenScrollBlock() {
        super(identifier, Arrangement.open);
    }


    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        return RotationHelper.rotateDirection(VoxelShapes.or(
                Block.makeCuboidShape(0.0, 0.0, 1.0, 2.0, 2.0, 15.0),
                Block.makeCuboidShape(14.0, 0.0, 1.0, 16.0, 2.0, 15.0),
                Block.makeCuboidShape(2.0, 0.0, 1.0, 14.0, 0.1, 15.0)), facing);
    }
}
