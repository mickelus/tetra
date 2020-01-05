package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;

import javax.annotation.Nullable;

public class CoreExtractorPipeBlock extends TetraBlock {
    public static final DirectionProperty facingProp = DirectionalBlock.FACING;
    public static final BooleanProperty poweredProp = BooleanProperty.create("powered");

    public static final String unlocalizedName = "extractor_pipe";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static CoreExtractorPipeBlock instance;

    public CoreExtractorPipeBlock() {
        super(ForgedBlockCommon.properties);
        setRegistryName(unlocalizedName);

        hasItem = true;
    }

    public static boolean isPowered(World world, BlockPos pos) {
        BlockState pipeState = world.getBlockState(pos);
        return instance.equals(pipeState.getBlock()) && pipeState.get(poweredProp);
    }

    private boolean shouldGetPower(World world, BlockPos pos, Direction blockFacing) {
        // iterate nearby blocks and look for a pipe that feeds power into this one, rather than having this automatically drain from the
        // pipe opposite of this pipes facing
        for (Direction facing : Direction.values()) {
            if (!facing.equals(blockFacing)) {
                BlockState adjacent = world.getBlockState(pos.offset(facing));
                if (adjacent.getBlock().equals(this)
                        && facing.equals(adjacent.get(facingProp).getOpposite())
                        && adjacent.get(poweredProp)) {
                    return true;
                }
            }
        }

        return SeepingBedrockBlock.isActive(world, pos.offset(blockFacing.getOpposite()));
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block fromBlock, BlockPos fromPos, boolean isMoving) {
        boolean getsPowered = shouldGetPower(world, pos, state.get(facingProp));

        if (state.get(poweredProp) != getsPowered) {
            world.setBlockState(pos, state.with(poweredProp, getsPowered));
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(facingProp, poweredProp);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context)
                .with(facingProp, context.getFace())
                .with(poweredProp, shouldGetPower(context.getWorld(), context.getPos(), context.getFace()));
    }

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation direction) {
        return state.with(facingProp, direction.rotate(state.get(facingProp)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.toRotation(state.get(facingProp)));
    }
}
