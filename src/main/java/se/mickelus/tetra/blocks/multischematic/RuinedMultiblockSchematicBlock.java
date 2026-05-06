package se.mickelus.tetra.blocks.multischematic;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;
import se.mickelus.tetra.TetraItemAbilities;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.effect.EffectHelper;

import java.util.Collection;

public class RuinedMultiblockSchematicBlock extends HorizontalDirectionalBlock implements IInteractiveBlock {
    public static final DirectionProperty facingProp = BlockStateProperties.HORIZONTAL_FACING;
    private final MapCodec<RuinedMultiblockSchematicBlock> codec = MapCodec.unit(this);

    protected ResourceLocation pryTable;

    protected BlockInteraction[] pryAction = new BlockInteraction[] {
            new BlockInteraction(TetraItemAbilities.pry, 1, Direction.EAST, 6, 10, 7, 10,
                    BlockStatePredicate.ANY,
                    this::pryBlock)
    };

    public RuinedMultiblockSchematicBlock(final Properties properties, ResourceLocation pryTable) {
        super(properties);
        this.pryTable = pryTable;

        this.registerDefaultState(this.stateDefinition.any().setValue(facingProp, Direction.EAST));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(facingProp);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        return state.setValue(facingProp, context.getHorizontalDirection().getOpposite());
    }

    private InteractionResult useInternal(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (pryTable != null) {
            return BlockInteraction.attemptInteraction(world, state, pos, player, hand, hit);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (pryTable != null) {
            return switch (useInternal(state, world, pos, player, hand, hit)) {
                case SUCCESS, CONSUME -> ItemInteractionResult.sidedSuccess(world.isClientSide);
                case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
                case FAIL -> ItemInteractionResult.FAIL;
                default -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            };
        }
        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (pryTable != null) {
            return useInternal(state, world, pos, player, InteractionHand.MAIN_HAND, hit);
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public BlockInteraction[] getPotentialInteractions(Level world, BlockPos pos, BlockState blockState, Direction face, Collection<ItemAbility> tools) {
        if (pryTable != null && face.getOpposite().equals(blockState.getValue(facingProp))) {
            return pryAction;
        }
        return new BlockInteraction[0];
    }

    protected boolean pryBlock(Level world, BlockPos pos, BlockState blockState, Player player, InteractionHand hand, Direction facing) {
        boolean didBreak = EffectHelper.breakBlock(world, player, player.getItemInHand(hand), pos, blockState, false, false);
        if (didBreak && world instanceof ServerLevel) {
            BlockInteraction.getLoot(pryTable, player, hand, (ServerLevel) world, blockState)
                    .forEach(lootStack -> popResource(world, pos, lootStack));
        }

        return true;
    }
}
