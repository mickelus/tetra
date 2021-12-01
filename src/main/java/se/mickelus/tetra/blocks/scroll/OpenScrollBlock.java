package se.mickelus.tetra.blocks.scroll;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
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
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        return RotationHelper.rotateDirection(Shapes.or(
                Block.box(0.0, 0.0, 1.0, 2.0, 2.0, 15.0),
                Block.box(14.0, 0.0, 1.0, 16.0, 2.0, 15.0),
                Block.box(2.0, 0.0, 1.0, 14.0, 0.1, 15.0)), facing);
    }
}
