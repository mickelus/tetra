package se.mickelus.tetra.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import se.mickelus.tetra.util.TileEntityOptional;

public class TetraBlock extends Block implements ITetraBlock {

    protected boolean hasItem = false;

    public TetraBlock(Block.Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasItem() {
        return hasItem;
    }

    public static void  dropBlockInventory(Block thisBlock, Level world, BlockPos pos, BlockState newState) {
        if (!thisBlock.equals(newState.getBlock())) {
            TileEntityOptional.from(world, pos, BlockEntity.class)
                    .map(te -> te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
                    .orElse(LazyOptional.empty())
                    .ifPresent(cap -> {
                        for (int i = 0; i < cap.getSlots(); i++) {
                            ItemStack itemStack = cap.getStackInSlot(i);
                            if (!itemStack.isEmpty()) {
                                Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemStack.copy());
                            }
                        }
                    });

            TileEntityOptional.from(world, pos, BlockEntity.class).ifPresent(BlockEntity::setRemoved);
        }
    }
}
