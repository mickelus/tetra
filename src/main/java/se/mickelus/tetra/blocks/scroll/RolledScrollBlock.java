package se.mickelus.tetra.blocks.scroll;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.util.RotationHelper;
import se.mickelus.tetra.util.TileEntityOptional;

import java.util.EnumMap;
import java.util.Map;

public class RolledScrollBlock extends ScrollBlock {
    public static final String identifier = "scroll_rolled";
    @ObjectHolder(TetraMod.MOD_ID + ":" + identifier)
    public static ScrollBlock instance;

    private final VoxelShape[] baseShapes = new VoxelShape[] {
            Block.box(6.0, 0.0, 1.0, 9.0, 3.0, 15.0),
            Block.box(4.0, 0.0, 1.0, 11.0, 3.0, 15.0),
            Block.box(2.0, 0.0, 1.0, 13.0, 3.0, 15.0),
            VoxelShapes.or(Block.box(2.0, 0.0, 1.0, 13.0, 3.0, 15.0),
                    Block.box(8.0, 3.0, 1.0, 11.0, 6.0, 15.0)),
            VoxelShapes.or(Block.box(2.0, 0.0, 1.0, 13.0, 3.0, 15.0),
                    Block.box(4.0, 3.0, 1.0, 11.0, 6.0, 15.0)),
            VoxelShapes.or(Block.box(2.0, 0.0, 1.0, 13.0, 3.0, 15.0),
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
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int index = TileEntityOptional.from(worldIn, pos, ScrollTile.class)
                .map(ScrollTile::getScrolls)
                .map(scrolls -> scrolls.length - 1)
                .map(c -> MathHelper.clamp(c, 0, 5))
                .orElse(0);

        return shapes.get(facing)[index];
    }
}
