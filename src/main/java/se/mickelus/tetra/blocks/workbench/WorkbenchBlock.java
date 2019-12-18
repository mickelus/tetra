package se.mickelus.tetra.blocks.workbench;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.advancements.BlockUseCriterion;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.workbench.action.ConfigAction;
import se.mickelus.tetra.blocks.workbench.action.WorkbenchActionPacket;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchScreen;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WorkbenchBlock extends TetraBlock implements ITileEntityProvider {
    public static final String unlocalizedName = "workbench";

    public static final AxisAlignedBB forgedAABB = new AxisAlignedBB(0.125, 0, 0, 0.875, 1, 1);

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static WorkbenchBlock instance;

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ContainerType<WorkbenchContainer> containerType;

    // todo 1.14: split off forged workbench into separate block
    public WorkbenchBlock() {
        super(Properties.create(Material.WOOD).hardnessAndResistance(2.5f));

        setRegistryName(unlocalizedName);

        hasItem = true;

        DataManager.actionData.onReload(() -> {
            WorkbenchTile.setupConfigActions(DataManager.actionData.getData().values().stream()
                    .flatMap(Arrays::stream).toArray(ConfigAction[]::new));
        });
    }
    @Override
    public void addInformation(ItemStack itemStack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        tooltip.add(new TranslationTextComponent("block.tetra.workbench.description"));
    }
    public static ActionResultType upgradeWorkbench(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction facing) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos.offset(facing), facing, itemStack)) {
            return ActionResultType.FAIL;
        }

        if (world.getBlockState(pos).getBlock().equals(Blocks.CRAFTING_TABLE)) {

            world.playSound(player, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 0.5F);

            if (!world.isRemote) {
                world.setBlockState(pos, instance.getDefaultState());

                BlockUseCriterion.trigger((ServerPlayerEntity) player, instance.getDefaultState(), ItemStack.EMPTY);
            }
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.PASS;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isRemote) {
            TileEntityOptional.from(world, pos, WorkbenchTile.class)
                    .ifPresent(te -> NetworkHooks.openGui((ServerPlayerEntity) player, te, pos));
        }
        return true;
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        TileEntityOptional.from(world, pos, WorkbenchTile.class)
                .map(te -> te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                .orElse(LazyOptional.empty())
                .ifPresent(cap -> {
                    for (int i = 0; i < cap.getSlots(); i++) {
                        ItemStack itemStack = cap.getStackInSlot(i);
                        if (!itemStack.isEmpty()) {
                            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemStack.copy());
                        }
                    }
                });

        TileEntityOptional.from(world, pos, WorkbenchTile.class).ifPresent(TileEntity::remove);
    }

    @Override
    public Collection<Capability> getCapabilities(World world, BlockPos pos, BlockState blockState) {
        BlockState accessoryBlockState = world.getBlockState(pos.offset(Direction.UP));
        if (accessoryBlockState.getBlock() instanceof ITetraBlock) {
            ITetraBlock block = (ITetraBlock) accessoryBlockState.getBlock();
            return block.getCapabilities(world, pos.offset(Direction.UP), accessoryBlockState);
        }
        return Collections.emptyList();
    }

    @Override
    public int getCapabilityLevel(World world, BlockPos pos, BlockState blockState, Capability capability) {
        BlockState accessoryBlockState = world.getBlockState(pos.offset(Direction.UP));
        if (accessoryBlockState.getBlock() instanceof ITetraBlock) {
            ITetraBlock block = (ITetraBlock) accessoryBlockState.getBlock();
            return block.getCapabilityLevel(world, pos.offset(Direction.UP), accessoryBlockState, capability);
        }
        return -1;
    }

//    @Override
//    public ItemStack onCraftConsumeCapability(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player, boolean consumeResources) {
//        BlockPos topPos = pos.offset(Direction.UP);
//        if (world.getBlockState(topPos).getBlock() instanceof BlockHammerHead) {
//            BlockHammerHead hammer = (BlockHammerHead) world.getBlockState(topPos).getBlock();
//            return hammer.onCraftConsumeCapability(world, topPos, world.getBlockState(topPos), targetStack, player, consumeResources);
//        }
//        return targetStack;
//    }
//
//    @Override
//    public ItemStack onActionConsumeCapability(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player, boolean consumeResources) {
//        BlockPos topPos = pos.offset(Direction.UP);
//        if (world.getBlockState(topPos).getBlock() instanceof BlockHammerHead) {
//            BlockHammerHead hammer = (BlockHammerHead) world.getBlockState(topPos).getBlock();
//            return hammer.onActionConsumeCapability(world, topPos, world.getBlockState(topPos), targetStack, player, consumeResources);
//        }
//        return targetStack;
//    }

    @Override
    public void init(PacketHandler packetHandler) {
        super.init(packetHandler);

        PacketHandler.instance.registerPacket(WorkbenchPacketUpdate.class, WorkbenchPacketUpdate::new);
        PacketHandler.instance.registerPacket(WorkbenchPacketCraft.class, WorkbenchPacketCraft::new);
        PacketHandler.instance.registerPacket(WorkbenchActionPacket.class, WorkbenchActionPacket::new);
        PacketHandler.instance.registerPacket(WorkbenchPacketTweak.class, WorkbenchPacketTweak::new);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void clientInit() {
        ClientRegistry.bindTileEntitySpecialRenderer(WorkbenchTile.class, new WorkbenchTESR());
        ScreenManager.registerFactory(containerType, WorkbenchScreen::new);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader world) {
        return new WorkbenchTile();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
