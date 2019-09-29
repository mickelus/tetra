package se.mickelus.tetra.items.toolbelt.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGlassBottle;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.items.toolbelt.ItemToolbeltModular;
import se.mickelus.tetra.items.toolbelt.SlotType;

import javax.annotation.Nullable;

public class InventoryPotions extends InventoryToolbelt {

    private static final String inventoryKey = "potionsInventory";
    public static int maxSize = 10; // 9;

    public InventoryPotions(ItemStack stack) {
        super(inventoryKey, stack, maxSize, SlotType.potion);
        ItemToolbeltModular item = (ItemToolbeltModular) stack.getItem();
        numSlots = item.getNumSlots(stack, SlotType.potion);

        predicate = InventoryToolbelt.potionPredicate;

        readFromNBT(NBTHelper.getTag(stack));
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
            if (ItemStack.areItemStackTagsEqual(storedStack, itemStack)
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
        itemStack = itemStack.splitStack(itemStack.getMaxStackSize());
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
                moveStackToPlayer(itemStack.splitStack(itemStack.getMaxStackSize()), player);
            }
        }
        markDirty();
    }


}
