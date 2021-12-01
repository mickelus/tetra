package se.mickelus.tetra.blocks.scroll;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
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
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumMap;
import java.util.Map;
@ParametersAreNonnullByDefault
public class RolledScrollBlock extends ScrollBlock {
    public static final String identifier = "scroll_rolled";
    @ObjectHolder(TetraMod.MOD_ID + ":" + identifier)
    public static ScrollBlock instance;

    private final VoxelShape[] baseShapes = new VoxelShape[] {
            Block.box(6.0, 0.0, 1.0, 9.0, 3.0, 15.0),
            Block.box(4.0, 0.0, 1.0, 11.0, 3.0, 15.0),
            Block.box(2.0, 0.0, 1.0, 13.0, 3.0, 15.0),
            Shapes.or(Block.box(2.0, 0.0, 1.0, 13.0, 3.0, 15.0),
                    Block.box(8.0, 3.0, 1.0, 11.0, 6.0, 15.0)),
            Shapes.or(Block.box(2.0, 0.0, 1.0, 13.0, 3.0, 15.0),
                    Block.box(4.0, 3.0, 1.0, 11.0, 6.0, 15.0)),
            Shapes.or(Block.box(2.0, 0.0, 1.0, 13.0, 3.0, 15.0),
                    Block.box(4.0, 3.0, 1.0, 11.0, 6.0, 15.0),
                    Block.box(6.0, 6.0, 1.0, 9.0, 9.0, 15.0))
    };
    private final Map<Direction, VoxelShape[]> shapes;

    public RolledScrollBlock() {
        super(identifier, Arrangement.rolled);

        shapes = new EnumMap<>(Direction.class);
        for (int i = 0; i < 4; i++) {
            Direction direction = Direction.from2DDataValue(i);

            VoxelShape[] result = new VoxelShape[baseShapes.length];
            for (int j = 0; j < result.length; j++) {
                result[j] = RotationHelper.rotateDirection(baseShapes[j], direction);
            }

            shapes.put(direction, result);
        }
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int index = TileEntityOptional.from(worldIn, pos, ScrollTile.class)
                .map(ScrollTile::getScrolls)
                .map(scrolls -> scrolls.length - 1)
                .map(c -> Mth.clamp(c, 0, 5))
                .orElse(0);

        return shapes.get(facing)[index];
    }
}
