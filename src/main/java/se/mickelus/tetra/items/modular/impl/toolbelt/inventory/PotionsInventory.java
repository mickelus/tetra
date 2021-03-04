package se.mickelus.tetra.items.modular.impl.toolbelt.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.modular.impl.toolbelt.ModularToolbeltItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.SlotType;

public class PotionsInventory extends ToolbeltInventory {

    private static final String inventoryKey = "potionsInventory";
    public static int maxSize = 10; // 9;

    public PotionsInventory(ItemStack stack) {
        super(inventoryKey, stack, maxSize, SlotType.potion);
        ModularToolbeltItem item = (ModularToolbeltItem) stack.getItem();
        numSlots = item.getNumSlots(stack, SlotType.potion);

        predicate = ToolbeltInventory.potionPredicate;

        readFromNBT(stack.getOrCreateTag());
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack itemStack) {
        return isItemValid(itemStack);
    }

    @Override
    public boolean storeItemInInventory(ItemStack itemStack) {
        if (!isItemValid(itemStack)) {
            return false;
        }

        // attempt to merge the itemstack with itemstacks in the inventory
        for (int i = 0; i < getSizeInventory(); i++) {
            ItemStack storedStack = getStackInSlot(i);
            if (ItemStack.areItemsEqual(storedStack, itemStack)
                    && ItemStack.areItemStackTagsEqual(storedStack, itemStack)
                    && storedStack.getCount() < 64) {

                int moveCount = Math.min(itemStack.getCount(), 64 - storedStack.getCount());
                storedStack.grow(moveCount);
                setInventorySlotContents(i, storedStack);
                itemStack.shrink(moveCount);

                if (itemStack.isEmpty()) {
                    return true;
                }
            }
        }

        // put item in the first empty slot
        for (int i = 0; i < getSizeInventory(); i++) {
            if (getStackInSlot(i).isEmpty()) {
                setInventorySlotContents(i, itemStack);
                return true;
            }
        }
        return false;
    }

    public ItemStack takeItemStack(int index) {
        ItemStack itemStack = getStackInSlot(index);
        itemStack = itemStack.split(itemStack.getMaxStackSize());
        if (getStackInSlot(index).isEmpty()) {
            setInventorySlotContents(index, ItemStack.EMPTY);
        } else {
            markDirty();
        }
        return itemStack;
    }

    @Override
    public void emptyOverflowSlots(PlayerEntity player) {
        for (int i = getSizeInventory(); i < maxSize; i++) {
            ItemStack itemStack = getStackInSlot(i);
            while (!itemStack.isEmpty()) {
                moveStackToPlayer(itemStack.split(itemStack.getMaxStackSize()), player);
            }
        }
        markDirty();
    }


}
