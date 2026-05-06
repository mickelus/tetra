package se.mickelus.tetra.blocks.rack;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import se.mickelus.mutil.util.ItemHandlerWrapper;
import se.mickelus.mutil.util.TileEntityOptional;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.blocks.IToolProviderBlock;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.properties.IToolProvider;
import se.mickelus.tetra.properties.PropertyHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@ParametersAreNonnullByDefault
public class RackBlock extends TetraWaterloggedBlock implements EntityBlock, IToolProviderBlock {
    public static final String identifier = "rack";
    public static final DirectionProperty facingProp = HorizontalDirectionalBlock.FACING;
    private static final Map<Direction, VoxelShape> shapes = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Block.box(0.0, 11.0, 14.0, 16.0, 14.0, 16.0),
            Direction.SOUTH, Block.box(0.0, 11.0, 0.0, 16.0, 14.0, 2.0),
            Direction.WEST, Block.box(14.0, 11.0, 0.0, 16.0, 14.0, 16.0),
            Direction.EAST, Block.box(0.0, 11.0, 0.0, 2.0, 14.0, 16.0)));
    public static RackBlock instance;


    public RackBlock() {
        super(Block.Properties.of()
                .strength(1.0F)
                .sound(SoundType.WOOD));
    }

    private static double getHitX(Direction facing, AABB boundingBox, double hitX, double hitY, double hitZ) {
        return switch (facing) {
            case NORTH -> boundingBox.maxX - hitX;
            case SOUTH -> hitX - boundingBox.minX;
            case WEST -> hitZ - boundingBox.minZ;
            case EAST -> boundingBox.maxZ - hitZ;
            default -> 0;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(facingProp);
    }

    private InteractionResult useInternal(BlockState blockState, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Direction facing = blockState.getValue(facingProp);
        AABB boundingBox = blockState.getShape(player.level(), pos).bounds();
        if (facing == hit.getDirection()) {
            Vec3 hitVec = hit.getLocation();
            int slot = getHitX(facing, boundingBox,
                    (float) hitVec.x - pos.getX(),
                    (float) hitVec.y - pos.getY(),
                    (float) hitVec.z - pos.getZ())
                    > 0.5 ? 1 : 0;

            TileEntityOptional.from(world, pos, RackTile.class)
                    .ifPresent(tile -> tile.slotInteract(slot, player, hand));

            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState blockState, Level world, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        return switch (useInternal(blockState, world, pos, player, hand, hit)) {
            case SUCCESS, CONSUME -> ItemInteractionResult.sidedSuccess(world.isClientSide);
            case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
            case FAIL -> ItemInteractionResult.FAIL;
            default -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        return useInternal(blockState, world, pos, player, InteractionHand.MAIN_HAND, hit);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (Direction.Axis.Y != context.getClickedFace().getAxis()) {
            return Optional.of(defaultBlockState().setValue(facingProp, context.getClickedFace()))
                    .filter(blockState -> blockState.canSurvive(context.getLevel(), context.getClickedPos()))
                    .orElse(null);
        }

        return null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        Direction facing = state.getValue(facingProp);
        BlockPos offsetPos = pos.relative(facing.getOpposite());
        return worldIn.getBlockState(offsetPos).isFaceSturdy(worldIn, offsetPos, facing);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        dropBlockInventory(this, world, pos, newState);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        return facing.getOpposite() == stateIn.getValue(facingProp) && !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return shapes.get(state.getValue(facingProp));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(facingProp, rot.rotate(state.getValue(facingProp)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return rotate(state, mirrorIn.getRotation(state.getValue(facingProp)));
    }


    @Override
    public void appendHoverText(final ItemStack stack, final net.minecraft.world.item.Item.TooltipContext context, final List<Component> tooltip,
            final TooltipFlag advanced) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);
            tooltip.add(Component.translatable("block.tetra.rack.description").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Tooltips.expand);
        }
    }

    @Override
    public boolean canProvideTools(Level world, BlockPos pos, BlockPos targetPos) {
        return true;
    }

    @Override
    public Collection<ItemAbility> getTools(Level world, BlockPos pos, BlockState blockState) {
        return TileEntityOptional.from(world, pos, RackTile.class)
                .map(tile -> tile.getItemHandler(null))
                .map(ItemHandlerWrapper::new)
                .map(PropertyHelper::getInventoryTools)
                .orElseGet(Collections::emptySet);
    }

    @Override
    public int getToolLevel(Level world, BlockPos pos, BlockState blockState, ItemAbility toolAction) {
        return TileEntityOptional.from(world, pos, RackTile.class)
                .map(tile -> tile.getItemHandler(null))
                .map(ItemHandlerWrapper::new)
                .map(inv -> PropertyHelper.getInventoryToolLevel(inv, toolAction))
                .orElse(-1);
    }

    @Override
    public ItemStack onCraftConsumeTool(Level world, BlockPos pos, BlockState blockState, ItemStack targetStack, String slot, boolean isReplacing,
            Player player, ItemAbility requiredTool, int requiredLevel, boolean consumeResources) {


        Optional<Container> optional = TileEntityOptional.from(world, pos, RackTile.class)
                .map(tile -> tile.getItemHandler(null))
                .map(ItemHandlerWrapper::new);

        if (optional.isPresent()) {
            Container inventory = optional.orElse(null);
            ItemStack providerStack = PropertyHelper.getInventoryProvidingItemStack(inventory, requiredTool, requiredLevel);

            if (!providerStack.isEmpty()) {
                if (consumeResources) {
                    spawnConsumeParticle(world, pos, blockState, inventory, providerStack);
                }

                return ((IToolProvider) providerStack.getItem())
                        .onCraftConsume(providerStack, targetStack, player, requiredTool, requiredLevel, consumeResources);
            }
        }

        return null;
    }

    @Override
    public ItemStack onActionConsumeTool(Level world, BlockPos pos, BlockState blockState, ItemStack targetStack, Player player,
            ItemAbility requiredTool, int requiredLevel, boolean consumeResources) {
        Optional<ItemHandlerWrapper> optional = TileEntityOptional.from(world, pos, RackTile.class)
                .map(tile -> tile.getItemHandler(null))
                .map(ItemHandlerWrapper::new);

        if (optional.isPresent()) {
            Container inventory = optional.orElse(null);
            ItemStack providerStack = PropertyHelper.getInventoryProvidingItemStack(inventory, requiredTool, requiredLevel);

            if (!providerStack.isEmpty()) {
                if (consumeResources) {
                    spawnConsumeParticle(world, pos, blockState, inventory, providerStack);
                }

                return ((IToolProvider) providerStack.getItem())
                        .onActionConsume(providerStack, targetStack, player, requiredTool, requiredLevel, consumeResources);
            }
        }

        return null;
    }

    /**
     * Spawns particles in the world indicating which tool was used.
     * todo: make this less nasty some day
     *
     * @param world
     * @param pos
     * @param blockState
     * @param inventory
     * @param providerStack
     */
    private void spawnConsumeParticle(Level world, BlockPos pos, BlockState blockState, Container inventory, ItemStack providerStack) {
        if (world instanceof ServerLevel) {
            Direction facing = blockState.getValue(RackBlock.facingProp);
            Vec3 particlePos = Vec3.atLowerCornerOf(pos).add(0.5f, 0.75f, 0.5f).add(Vec3.atLowerCornerOf(facing.getNormal()).scale(-0.3));

            ItemStack firstSlot = inventory.getItem(0);
            firstSlot = Optional.of(ItemUpgradeRegistry.instance.getReplacement(firstSlot))
                    .filter(itemStack -> !itemStack.isEmpty())
                    .orElse(firstSlot);
            if (ItemStack.matches(providerStack, firstSlot)) {
                particlePos = particlePos.add(Vec3.atLowerCornerOf(facing.getCounterClockWise().getNormal()).scale(-0.25));
            } else {
                particlePos = particlePos.add(Vec3.atLowerCornerOf(facing.getCounterClockWise().getNormal()).scale(0.25));
            }

            ((ServerLevel) world).sendParticles(new DustParticleOptions(new Vector3f(0.0f, 0.66f, 0.66f), 1f), particlePos.x(), particlePos.y(), particlePos.z(), 2, 0, 0, 0, 0f);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new RackTile(p_153215_, p_153216_);
    }
}
