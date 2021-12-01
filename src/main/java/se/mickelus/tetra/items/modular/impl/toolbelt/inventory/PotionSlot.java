package se.mickelus.tetra.items.modular.impl.toolbelt.inventory;

import net.minecraft.item.ItemStack;

public class PotionSlot extends PredicateSlot {
    public PotionSlot(PotionsInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y, inventory::isItemValid);
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 64;
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack currentItem = getItem();
        if (!currentItem.isEmpty()) {
            return super.remove(Math.min(currentItem.getMaxStackSize(), amount));
        }
        return super.remove(amount);
    }

    //    @Override
//    public ItemStack onTake(PlayerEntity thePlayer, ItemStack itemStack) {
//        ItemStack takenStack = itemStack.splitStack(itemStack.getItem().getItemStackLimit(itemStack));
//        onSlotChanged();
//        return takenStack;
//    }
}
