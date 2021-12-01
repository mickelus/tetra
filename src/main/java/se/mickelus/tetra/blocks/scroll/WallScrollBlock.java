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

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumMap;
import java.util.Map;
@ParametersAreNonnullByDefault
public class WallScrollBlock extends ScrollBlock {
    public static final String identifier = "scroll_wall";
    @ObjectHolder(TetraMod.MOD_ID + ":" + identifier)
    public static ScrollBlock instance;

    private VoxelShape baseShape = Shapes.or(
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
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return shapes.get(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
    }
}
