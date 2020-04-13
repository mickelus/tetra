package se.mickelus.tetra.blocks.workbench;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.TetraBlock;
import se.mickelus.tetra.blocks.forged.hammer.HammerHeadBlock;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

public abstract class AbstractWorkbenchBlock extends TetraBlock {
    public AbstractWorkbenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isRemote) {
            TileEntityOptional.from(world, pos, WorkbenchTile.class)
                    .ifPresent(te -> NetworkHooks.openGui((ServerPlayerEntity) player, te, pos));
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!equals(newState.getBlock())) {
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

    @Override
    public ItemStack onCraftConsumeCapability(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player,
            boolean consumeResources) {
        BlockPos topPos = pos.offset(Direction.UP);
        if (world.getBlockState(topPos).getBlock() instanceof HammerHeadBlock) {
            HammerHeadBlock hammer = (HammerHeadBlock) world.getBlockState(topPos).getBlock();
            return hammer.onCraftConsumeCapability(world, topPos, world.getBlockState(topPos), targetStack, player, consumeResources);
        }
        return targetStack;
    }

    @Override
    public ItemStack onActionConsumeCapability(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player,
            boolean consumeResources) {
        BlockPos topPos = pos.offset(Direction.UP);
        if (world.getBlockState(topPos).getBlock() instanceof HammerHeadBlock) {
            HammerHeadBlock hammer = (HammerHeadBlock) world.getBlockState(topPos).getBlock();
            return hammer.onActionConsumeCapability(world, topPos, world.getBlockState(topPos), targetStack, player, consumeResources);
        }
        return targetStack;
    }

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return new WorkbenchTile();
    }
}
