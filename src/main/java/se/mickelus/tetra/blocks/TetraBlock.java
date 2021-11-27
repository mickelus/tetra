package se.mickelus.tetra.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

    public static void  dropBlockInventory(Block thisBlock, World world, BlockPos pos, BlockState newState) {
        if (!thisBlock.equals(newState.getBlock())) {
            TileEntityOptional.from(world, pos, TileEntity.class)
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

            TileEntityOptional.from(world, pos, TileEntity.class).ifPresent(TileEntity::remove);
        }
    }
}
