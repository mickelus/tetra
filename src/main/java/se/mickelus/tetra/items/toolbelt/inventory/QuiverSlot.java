package se.mickelus.tetra.items.toolbelt.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class QuiverSlot extends Slot {
    public QuiverSlot(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isItemValid(@Nullable ItemStack itemStack) {
        return InventoryQuiver.isItemValid(itemStack);
    }
}
