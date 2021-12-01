package se.mickelus.tetra.blocks.rack;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.properties.PropertyHelper;
import se.mickelus.tetra.properties.IToolProvider;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.util.ItemHandlerWrapper;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.*;

public class RackBlock extends TetraWaterloggedBlock {
    public static final String unlocalizedName = "rack";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static RackBlock instance;

    public static final DirectionProperty facingProp = HorizontalBlock.FACING;

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
        ClientRegistry.bindTileEntityRenderer(RackTile.type, RackTESR::new);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(facingProp);
    }

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return new RackTile();
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        Direction facing = blockState.getValue(facingProp);
        AxisAlignedBB boundingBox = blockState.getShape(player.level, pos).bounds();
        if (facing == hit.getDirection()) {
            Vector3d hitVec = hit.getLocation();
            int slot = getHitX(facing, boundingBox,
                    (float) hitVec.x - pos.getX(),
                    (float) hitVec.y - pos.getY(),
                    (float) hitVec.z - pos.getZ())
                    > 0.5 ? 1 : 0;

            TileEntityOptional.from(world, pos, RackTile.class)
                    .ifPresent(tile -> tile.slotInteract(slot, player, hand));

            return ActionResultType.sidedSuccess(world.isClientSide);
        }

        return ActionResultType.PASS;
    }

    private static double getHitX(Direction facing, AxisAlignedBB boundingBox, double hitX, double hitY, double hitZ) {
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
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if (Direction.Axis.Y != context.getClickedFace().getAxis()) {
            return Optional.of(defaultBlockState().setValue(facingProp, context.getClickedFace()))
                    .filter(blockState -> blockState.canSurvive(context.getLevel(), context.getClickedPos()))
                    .orElse(null);
        }

        return null;
    }

    @Override
    public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.relative(state.getValue(facingProp).getOpposite())).getMaterial().isSolid();
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        dropBlockInventory(this, world, pos, newState);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return facing.getOpposite() == stateIn.getValue(facingProp) && !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
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
    public void appendHoverText(final ItemStack stack, @Nullable final IBlockReader world, final List<ITextComponent> tooltip,
            final ITooltipFlag advanced) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);
            tooltip.add(new TranslationTextComponent("block.tetra.rack.description").withStyle(TextFormatting.GRAY));
        } else {
            tooltip.add(Tooltips.expand);
        }
    }

    @Override
    public boolean canProvideTools(World world, BlockPos pos, BlockPos targetPos) {
        return true;
    }

    @Override
    public Collection<ToolType> getTools(World world, BlockPos pos, BlockState blockState) {
        return Optional.ofNullable(world.getBlockEntity(pos))
                .map(te -> te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                .orElse(LazyOptional.empty())
                .map(ItemHandlerWrapper::new)
                .map(PropertyHelper::getInventoryTools)
                .orElseGet(Collections::emptySet);
    }

    @Override
    public int getToolLevel(World world, BlockPos pos, BlockState blockState, ToolType toolType) {
        return Optional.ofNullable(world.getBlockEntity(pos))
                .map(te -> te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                .orElse(LazyOptional.empty())
                .map(ItemHandlerWrapper::new)
                .map(inv -> PropertyHelper.getInventoryToolLevel(inv, toolType))
                .orElse(-1);
    }

    @Override
    public ItemStack onCraftConsumeTool(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, String slot, boolean isReplacing,
            PlayerEntity player, ToolType requiredTool, int requiredLevel, boolean consumeResources) {


        Optional<IInventory> optional = Optional.ofNullable(world.getBlockEntity(pos))
                .map(te -> te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                .orElse(LazyOptional.empty())
                .map(ItemHandlerWrapper::new);

        if (optional.isPresent() && player != null) {
            IInventory inventory = optional.orElse(null);
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
    public ItemStack onActionConsumeTool(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player,
            ToolType requiredTool, int requiredLevel, boolean consumeResources) {
        Optional<ItemHandlerWrapper> optional = Optional.ofNullable(world.getBlockEntity(pos))
                .map(te -> te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                .orElse(LazyOptional.empty())
                .map(ItemHandlerWrapper::new);

        if (optional.isPresent() && player != null) {
            IInventory inventory = optional.orElse(null);
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
    private void spawnConsumeParticle(World world, BlockPos pos, BlockState blockState, IInventory inventory, ItemStack providerStack) {
        if (world instanceof ServerWorld) {
            Direction facing = blockState.getValue(RackBlock.facingProp);
            Vector3d particlePos = Vector3d.atLowerCornerOf(pos).add(0.5f, 0.75f, 0.5f).add(Vector3d.atLowerCornerOf(facing.getNormal()).scale(-0.3));

            ItemStack firstSlot = inventory.getItem(0);
            firstSlot = Optional.of(ItemUpgradeRegistry.instance.getReplacement(firstSlot))
                    .filter(itemStack -> !itemStack.isEmpty())
                    .orElse(firstSlot);
            if (ItemStack.matches(providerStack, firstSlot)) {
                particlePos = particlePos.add(Vector3d.atLowerCornerOf(facing.getCounterClockWise().getNormal()).scale(-0.25));
            } else {
                particlePos = particlePos.add(Vector3d.atLowerCornerOf(facing.getCounterClockWise().getNormal()).scale(0.25));
            }

            ((ServerWorld) world).sendParticles(new RedstoneParticleData(0.0f, 0.66f, 0.66f, 1f), particlePos.x(), particlePos.y(), particlePos.z(), 2, 0, 0, 0, 0f);
        }
    }
}
