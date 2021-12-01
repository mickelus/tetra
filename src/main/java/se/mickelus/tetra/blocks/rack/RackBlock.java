package se.mickelus.tetra.blocks.rack;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.properties.IToolProvider;
import se.mickelus.tetra.properties.PropertyHelper;
import se.mickelus.tetra.util.ItemHandlerWrapper;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
@ParametersAreNonnullByDefault
public class RackBlock extends TetraWaterloggedBlock implements EntityBlock {
    public static final String unlocalizedName = "rack";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static RackBlock instance;

    public static final DirectionProperty facingProp = HorizontalDirectionalBlock.FACING;

    private static final Map<Direction, VoxelShape> shapes = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Block.box(0.0, 11.0, 14.0, 16.0, 14.0, 16.0),
            Direction.SOUTH, Block.box(0.0, 11.0, 0.0, 16.0, 14.0, 2.0),
            Direction.WEST, Block.box(14.0, 11.0, 0.0, 16.0, 14.0, 16.0),
            Direction.EAST, Block.box( 0.0, 11.0, 0.0, 2.0, 14.0, 16.0)));


    public RackBlock() {
        super(Block.Properties.of(Material.WOOD, MaterialColor.WOOD)
                .strength(1.0F)
                .sound(SoundType.WOOD));

        hasItem = true;

        setRegistryName(unlocalizedName);
    }

    @Override
    public void clientInit() {
        BlockEntityRenderers.register(RackTile.type, RackTESR::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(facingProp);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Direction facing = blockState.getValue(facingProp);
        AABB boundingBox = blockState.getShape(player.level, pos).bounds();
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

    private static double getHitX(Direction facing, AABB boundingBox, double hitX, double hitY, double hitZ) {
        switch (facing) {
            case NORTH:
                return boundingBox.maxX - hitX;
            case SOUTH:
                return hitX - boundingBox.minX;
            case WEST:
                return hitZ - boundingBox.minZ;
            case EAST:
                return boundingBox.maxZ - hitZ;
        }
        return 0;
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
        return worldIn.getBlockState(pos.relative(state.getValue(facingProp).getOpposite())).getMaterial().isSolid();
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
        return state.rotate(mirrorIn.getRotation(state.getValue(facingProp)));
    }


    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final BlockGetter world, final List<Component> tooltip,
            final TooltipFlag advanced) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);
            tooltip.add(new TranslatableComponent("block.tetra.rack.description").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Tooltips.expand);
        }
    }

    @Override
    public boolean canProvideTools(Level world, BlockPos pos, BlockPos targetPos) {
        return true;
    }

    @Override
    public Collection<ToolAction> getTools(Level world, BlockPos pos, BlockState blockState) {
        return Optional.ofNullable(world.getBlockEntity(pos))
                .map(te -> te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                .orElse(LazyOptional.empty())
                .map(ItemHandlerWrapper::new)
                .map(PropertyHelper::getInventoryTools)
                .orElseGet(Collections::emptySet);
    }

    @Override
    public int getToolLevel(Level world, BlockPos pos, BlockState blockState, ToolAction toolType) {
        return Optional.ofNullable(world.getBlockEntity(pos))
                .map(te -> te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                .orElse(LazyOptional.empty())
                .map(ItemHandlerWrapper::new)
                .map(inv -> PropertyHelper.getInventoryToolLevel(inv, toolType))
                .orElse(-1);
    }

    @Override
    public ItemStack onCraftConsumeTool(Level world, BlockPos pos, BlockState blockState, ItemStack targetStack, String slot, boolean isReplacing,
            Player player, ToolAction requiredTool, int requiredLevel, boolean consumeResources) {


        Optional<Container> optional = Optional.ofNullable(world.getBlockEntity(pos))
                .map(te -> te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                .orElse(LazyOptional.empty())
                .map(ItemHandlerWrapper::new);

        if (optional.isPresent() && player != null) {
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
            ToolAction requiredTool, int requiredLevel, boolean consumeResources) {
        Optional<ItemHandlerWrapper> optional = Optional.ofNullable(world.getBlockEntity(pos))
                .map(te -> te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                .orElse(LazyOptional.empty())
                .map(ItemHandlerWrapper::new);

        if (optional.isPresent() && player != null) {
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

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return new RackTile(p_153215_, p_153216_);
    }
}
