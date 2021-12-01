package se.mickelus.tetra.items.modular.impl.toolbelt.inventory;

import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.impl.toolbelt.ModularToolbeltItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.SlotType;

import java.util.Collection;
import java.util.List;

public class StorageInventory extends ToolbeltInventory {

    private static final String inventoryKey = "storageInventory";
    public static int maxSize = 36; // 24;

    public StorageInventory(ItemStack stack) {
        super(inventoryKey, stack, maxSize, SlotType.storage);
        ModularToolbeltItem item = (ModularToolbeltItem) stack.getItem();
        numSlots = item.getNumSlots(stack, SlotType.storage);

        readFromNBT(stack.getOrCreateTag());
    }

    @Override
    public boolean storeItemInInventory(ItemStack itemStack) {
        if (!isItemValid(itemStack)) {
            return false;
        }

        List<Collection<ItemEffect>> effects = getSlotEffects();
        // attempt to merge the itemstack with itemstacks in the toolbelt
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack storedStack = getItem(i);
            if (effects.get(i).contains(ItemEffect.quickAccess)
                    && storedStack.sameItem(itemStack)
                    && storedStack.getCount() < storedStack.getMaxStackSize()) {

                int moveCount = Math.min(itemStack.getCount(), storedStack.getMaxStackSize() - storedStack.getCount());
                storedStack.grow(moveCount);
                setItem(i, storedStack);
                itemStack.shrink(moveCount);

                if (itemStack.isEmpty()) {
                    return true;
                }
            }
        }

        // put item in the first empty slot
        for (int i = 0; i < getContainerSize(); i++) {
            if (effects.get(i).contains(ItemEffect.quickAccess) && getItem(i).isEmpty()) {
                setItem(i, itemStack);
                return true;
            }
        }
        return false;
    }

    public static int getColumns(int slotCount) {
        for (int i = 12; i >= 5; i--) {
            if (slotCount % i == 0) {
                return i;
            }

        }
        return 9;
    }
}
