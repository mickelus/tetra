package se.mickelus.tetra.blocks.workbench;

import com.mojang.datafixers.util.Pair;
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
import se.mickelus.tetra.capabilities.ICapabilityProvider;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

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
        return BlockPos.getAllInBox(pos.add(-1, 0, -1), pos.add(1, 2, 1))
                .map(offsetPos -> new Pair<>(offsetPos, world.getBlockState(offsetPos)))
                .filter(pair -> pair.getSecond().getBlock() instanceof ITetraBlock)
                .filter(pair -> ((ITetraBlock) pair.getSecond().getBlock()).canProvideCapabilities(world, pair.getFirst(), pos))
                .map(pair -> ((ITetraBlock) pair.getSecond().getBlock()).getCapabilities(world, pair.getFirst(), pair.getSecond()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    @Override
    public int getCapabilityLevel(World world, BlockPos pos, BlockState blockState, Capability capability) {
        return BlockPos.getAllInBox(pos.add(-1, 0, -1), pos.add(1, 2, 1))
                .map(offsetPos -> new Pair<>(offsetPos, world.getBlockState(offsetPos)))
                .filter(pair -> pair.getSecond().getBlock() instanceof ITetraBlock)
                .filter(pair -> ((ITetraBlock) pair.getSecond().getBlock()).canProvideCapabilities(world, pair.getFirst(), pos))
                .map(pair -> ((ITetraBlock) pair.getSecond().getBlock()).getCapabilityLevel(world, pair.getFirst(), pair.getSecond(), capability))
                .max(Integer::compare)
                .orElse(-1);
    }

    private Pair<BlockPos, BlockState> getProvidingBlockstate(World world, BlockPos pos, BlockState blockState, ItemStack targetStack,
            Capability capability, int level) {
        return BlockPos.getAllInBox(pos.add(-1, 0, -1), pos.add(1, 2, 1))
                .map(offsetPos -> new Pair<>(offsetPos, world.getBlockState(offsetPos)))
                .filter(pair -> pair.getSecond().getBlock() instanceof ITetraBlock)
                .filter(pair -> ((ITetraBlock) pair.getSecond().getBlock()).canProvideCapabilities(world, pair.getFirst(), pos))
                .filter(pair -> ((ITetraBlock) pair.getSecond().getBlock()).getCapabilityLevel(world, pair.getFirst(), pair.getSecond(), capability) >= level)
                .findFirst()
                .orElse(null);
    }

    @Override
    public ItemStack onCraftConsumeCapability(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player,
            Capability requiredCapability, int requiredLevel, boolean consumeResources) {
        Pair<BlockPos, BlockState> provider = getProvidingBlockstate(world, pos, blockState, targetStack, requiredCapability, requiredLevel);

        if (provider != null) {
            ITetraBlock block = ((ITetraBlock) provider.getSecond().getBlock());
            return block.onCraftConsumeCapability(world, provider.getFirst(), provider.getSecond(), targetStack, player, requiredCapability,
                    requiredLevel, consumeResources);
        }

        return null;
    }

    @Override
    public ItemStack onActionConsumeCapability(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player,
            Capability requiredCapability, int requiredLevel, boolean consumeResources) {
        Pair<BlockPos, BlockState> provider = getProvidingBlockstate(world, pos, blockState, targetStack, requiredCapability, requiredLevel);

        if (provider != null) {
            ITetraBlock block = ((ITetraBlock) provider.getSecond().getBlock());
            return block.onActionConsumeCapability(world, provider.getFirst(), provider.getSecond(), targetStack, player, requiredCapability,
                    requiredLevel, consumeResources);
        }

        return null;
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
