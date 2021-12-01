package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(ForgedBlockCommon.locationTooltip);
    }

    public static boolean isPowered(Level world, BlockPos pos) {
        BlockState pipeState = world.getBlockState(pos);
        return instance.equals(pipeState.getBlock()) && pipeState.getValue(poweredProp);
    }

    private boolean shouldGetPower(Level world, BlockPos pos, Direction blockFacing) {
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
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block fromBlock, BlockPos fromPos, boolean isMoving) {
        boolean getsPowered = shouldGetPower(world, pos, state.getValue(facingProp));

        if (state.getValue(poweredProp) != getsPowered) {
            world.setBlockAndUpdate(pos, state.setValue(poweredProp, getsPowered));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(facingProp, poweredProp);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
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
