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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.blocks.TetraWaterloggedBlock;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.capabilities.ICapabilityProvider;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.util.ItemHandlerWrapper;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.*;

public class RackBlock extends TetraWaterloggedBlock {
    public static final String unlocalizedName = "rack";
    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static RackBlock instance;

    public static final DirectionProperty facingProp = HorizontalBlock.HORIZONTAL_FACING;

    private static final Map<Direction, VoxelShape> shapes = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Block.makeCuboidShape(0.0, 11.0, 14.0, 16.0, 14.0, 16.0),
            Direction.SOUTH, Block.makeCuboidShape(0.0, 11.0, 0.0, 16.0, 14.0, 2.0),
            Direction.WEST, Block.makeCuboidShape(14.0, 11.0, 0.0, 16.0, 14.0, 16.0),
            Direction.EAST, Block.makeCuboidShape( 0.0, 11.0, 0.0, 2.0, 14.0, 16.0)));


    public RackBlock() {
        super(Block.Properties.create(Material.WOOD, MaterialColor.WOOD)
                .hardnessAndResistance(1.0F)
                .sound(SoundType.WOOD));

        hasItem = true;

        setRegistryName(unlocalizedName);
    }

    @Override
    public void clientInit() {
        ClientRegistry.bindTileEntityRenderer(RackTile.type, RackTESR::new);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
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
    public ActionResultType onBlockActivated(BlockState blockState, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        Direction facing = blockState.get(facingProp);
        if (facing == hit.getFace()) {
            System.out.println(hit.getHitVec());
            int slot = getHitX(facing, hit.getHitVec(), pos) > 0.5 ? 1 : 0;

            TileEntityOptional.from(world, pos, RackTile.class)
                    .ifPresent(tile -> tile.slotInteract(slot, player, hand));

            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
    }

    private double getHitX(Direction facing, Vec3d hitVector, BlockPos blockPos) {
        switch (facing) {
            case WEST:
                return Math.abs(hitVector.getZ() % 1);
            case EAST:
                return 1 - Math.abs(hitVector.getZ() % 1);
            case NORTH:
                return Math.abs(hitVector.getX() % 1);
            case SOUTH:
                return 1 - Math.abs(hitVector.getX() % 1);
        }
        return 0;
    }


    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return Optional.of(getDefaultState().with(facingProp, context.getFace()))
                .filter(blockState -> blockState.isValidPosition(context.getWorld(), context.getPos()))
                .orElse(null);
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.offset(state.get(facingProp).getOpposite())).getMaterial().isSolid();
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        dropBlockInventory(this, world, pos, newState);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return facing.getOpposite() == stateIn.get(facingProp) && !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : stateIn;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return shapes.get(state.get(facingProp));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(facingProp, rot.rotate(state.get(facingProp)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(facingProp)));
    }


    @Override
    public void addInformation(final ItemStack stack, @Nullable final IBlockReader world, final List<ITextComponent> tooltip,
            final ITooltipFlag advanced) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Tooltips.expanded);
            tooltip.add(new TranslationTextComponent("block.tetra.rack.description").setStyle(new Style()
                    .setColor(TextFormatting.GRAY)));
        } else {
            tooltip.add(Tooltips.expand);
        }
    }

    @Override
    public boolean canProvideCapabilities(World world, BlockPos pos, BlockPos targetPos) {
        return true;
    }

    @Override
    public Collection<Capability> getCapabilities(World world, BlockPos pos, BlockState blockState) {
        return Optional.ofNullable(world.getTileEntity(pos))
                .map(te -> te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                .orElse(LazyOptional.empty())
                .map(ItemHandlerWrapper::new)
                .map(CapabilityHelper::getInventoryCapabilities)
                .orElseGet(Collections::emptySet);
    }

    @Override
    public int getCapabilityLevel(World world, BlockPos pos, BlockState blockState, Capability capability) {
        return Optional.ofNullable(world.getTileEntity(pos))
                .map(te -> te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                .orElse(LazyOptional.empty())
                .map(ItemHandlerWrapper::new)
                .map(inv -> CapabilityHelper.getInventoryCapabilityLevel(inv, capability))
                .orElse(-1);
    }

    @Override
    public ItemStack onCraftConsumeCapability(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player,
            Capability requiredCapability, int requiredLevel, boolean consumeResources) {


        LazyOptional<IInventory> optional = Optional.ofNullable(world.getTileEntity(pos))
                .map(te -> te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                .orElse(LazyOptional.empty())
                .map(ItemHandlerWrapper::new);

        if (optional.isPresent()) {
            IInventory inventory = optional.orElse(null);
            ItemStack providerStack = CapabilityHelper.getInventoryProvidingItemStack(inventory, requiredCapability, requiredLevel);

            if (!providerStack.isEmpty()) {
                if (consumeResources) {
                    spawnConsumeParticle(world, pos, blockState, inventory, providerStack);
                }

                return ((ICapabilityProvider) providerStack.getItem())
                        .onCraftConsumeCapability(providerStack, targetStack, player, requiredCapability, requiredLevel, consumeResources);
            }
        }

        return null;
    }

    @Override
    public ItemStack onActionConsumeCapability(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player,
            Capability requiredCapability, int requiredLevel, boolean consumeResources) {
        return Optional.ofNullable(world.getTileEntity(pos))
                .map(te -> te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                .orElse(LazyOptional.empty())
                .map(ItemHandlerWrapper::new)
                .map(inv -> CapabilityHelper.getInventoryProvidingItemStack(inv, requiredCapability, requiredLevel))
                .map(providerStack -> ((ICapabilityProvider) providerStack.getItem()).onActionConsumeCapability(providerStack, targetStack, player, requiredCapability, requiredLevel, consumeResources))
                .orElse(null);
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
            Direction facing = blockState.get(RackBlock.facingProp);
            Vec3d particlePos = new Vec3d(pos).add(0.5f, 0.75f, 0.5f).add(new Vec3d(facing.getDirectionVec()).scale(-0.3));

            ItemStack firstSlot = inventory.getStackInSlot(0);
            firstSlot = Optional.of(ItemUpgradeRegistry.instance.getReplacement(firstSlot))
                    .filter(itemStack -> !itemStack.isEmpty())
                    .orElse(firstSlot);
            if (ItemStack.areItemStacksEqual(providerStack, firstSlot)) {
                particlePos = particlePos.add(new Vec3d(facing.rotateYCCW().getDirectionVec()).scale(-0.25));
            } else {
                particlePos = particlePos.add(new Vec3d(facing.rotateYCCW().getDirectionVec()).scale(0.25));
            }

            ((ServerWorld) world).spawnParticle(new RedstoneParticleData(0.0f, 0.66f, 0.66f, 1f), particlePos.getX(), particlePos.getY(), particlePos.getZ(), 2, 0, 0, 0, 0f);
        }
    }
}
