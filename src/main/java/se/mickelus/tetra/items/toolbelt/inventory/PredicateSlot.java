package se.mickelus.tetra.items.toolbelt.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class PredicateSlot extends Slot {

    protected Predicate<ItemStack> predicate;

    public PredicateSlot(IInventory inventory, int index, int x, int y, Predicate<ItemStack> predicate) {
        super(inventory, index, x, y);

        this.predicate = predicate;
    }

    @Override
    public boolean isItemValid(@Nullable ItemStack itemStack) {
        return itemStack != null && predicate.test(itemStack);
    }
}
