package se.mickelus.tetra.items.toolbelt.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemGlassBottle;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class PotionSlot extends Slot {
    public PotionSlot(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isItemValid(@Nullable ItemStack itemStack) {
        return InventoryPotions.isItemValid(itemStack);
    }


    @Override
    public int getSlotStackLimit() {
        return 64;
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 64;
    }

    @Override
    public ItemStack decrStackSize(int amount) {
        ItemStack currentItem = getStack();
        if (!currentItem.isEmpty()) {
            return super.decrStackSize(Math.min(currentItem.getMaxStackSize(), amount));
        }
        return super.decrStackSize(amount);
    }

    //    @Override
//    public ItemStack onTake(EntityPlayer thePlayer, ItemStack itemStack) {
//        ItemStack takenStack = itemStack.splitStack(itemStack.getItem().getItemStackLimit(itemStack));
//        onSlotChanged();
//        return takenStack;
//    }
}
