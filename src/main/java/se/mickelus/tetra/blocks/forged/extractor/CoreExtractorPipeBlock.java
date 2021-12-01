package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.forged.ForgedBlockCommon;

import javax.annotation.Nullable;
import java.util.List;

public class CoreExtractorPipeBlock extends TetraBlock {
    public static final DirectionProperty facingProp = BlockStateProperties.FACING;
    public static final BooleanProperty poweredProp = BooleanProperty.create("powered");

    public static final String unlocalizedName = "extractor_pipe";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static CoreExtractorPipeBlock instance;

    public CoreExtractorPipeBlock() {
        super(ForgedBlockCommon.propertiesSolid);
        setRegistryName(unlocalizedName);

        hasItem = true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }

    public static boolean isPowered(World world, BlockPos pos) {
        BlockState pipeState = world.getBlockState(pos);
        return instance.equals(pipeState.getBlock()) && pipeState.getValue(poweredProp);
    }

    private boolean shouldGetPower(World world, BlockPos pos, Direction blockFacing) {
        // iterate nearby blocks and look for a pipe that feeds power into this one, rather than having this automatically drain from the
        // pipe opposite of this pipes facing
        for (Direction facing : Direction.values()) {
            if (!facing.equals(blockFacing)) {
                BlockState adjacent = world.getBlockState(pos.relative(facing));
                if (adjacent.getBlock().equals(this)
                        && facing.equals(adjacent.getValue(facingProp).getOpposite())
                        && adjacent.getValue(poweredProp)) {
                    return true;
                }
            }
        }

        return SeepingBedrockBlock.isActive(world, pos.relative(blockFacing.getOpposite()));
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block fromBlock, BlockPos fromPos, boolean isMoving) {
        boolean getsPowered = shouldGetPower(world, pos, state.getValue(facingProp));

        if (state.getValue(poweredProp) != getsPowered) {
            world.setBlockAndUpdate(pos, state.setValue(poweredProp, getsPowered));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(facingProp, poweredProp);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context)
                .setValue(facingProp, context.getClickedFace())
                .setValue(poweredProp, shouldGetPower(context.getLevel(), context.getClickedPos(), context.getClickedFace()));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation direction) {
        return state.setValue(facingProp, direction.rotate(state.getValue(facingProp)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(facingProp)));
    }
}
