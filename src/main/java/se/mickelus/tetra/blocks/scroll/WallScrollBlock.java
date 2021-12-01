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

import java.util.EnumMap;
import java.util.Map;

public class WallScrollBlock extends ScrollBlock {
    public static final String identifier = "scroll_wall";
    @ObjectHolder(TetraMod.MOD_ID + ":" + identifier)
    public static ScrollBlock instance;

    private VoxelShape baseShape = VoxelShapes.or(
            Block.box(1.0, 14.0, 0.0, 15.0, 16.0, 2.0),
            Block.box(1.0, 1.0, 0.0, 15.0, 14.0, 0.1));
    private final Map<Direction, VoxelShape> shapes;

    public WallScrollBlock() {
        super(identifier, Arrangement.wall);

        shapes = new EnumMap<>(Direction.class);
        for (int i = 0; i < 4; i++) {
            Direction direction = Direction.from2DDataValue(i);
            shapes.put(direction, RotationHelper.rotateDirection(baseShape, direction));
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return shapes.get(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
    }
}
