package se.mickelus.tetra.items.toolbelt.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.items.toolbelt.ItemToolbeltModular;

public class InventoryQuickslot extends InventoryToolbelt {

    public static final int maxSize = 12;
    private static final String inventoryKey = "quickInventory";
    private static final String shadowsKey = "quickShadows";


    private NonNullList<ItemStack> inventoryShadows;

    public InventoryQuickslot(ItemStack stack) {
        super(inventoryKey, stack, maxSize);
        ItemToolbeltModular item = (ItemToolbeltModular) stack.getItem();
        numSlots = item.getNumQuickslots(stack);

        inventoryShadows = NonNullList.withSize(maxSize, ItemStack.EMPTY);

        readFromNBT(NBTHelper.getTag(stack));
    }


    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        NBTTagList shadows = tagCompound.getTagList(shadowsKey, Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < shadows.tagCount(); i++) {
            NBTTagCompound item = shadows.getCompoundTagAt(i);
            int slot = item.getInteger(slotKey);

            if (0 <= slot && slot < getSizeInventory()) {
                inventoryShadows.set(slot, new ItemStack(item));
            }
        }
    }

    public void writeToNBT(NBTTagCompound tagcompound) {
        super.writeToNBT(tagcompound);
        NBTTagList shadows = new NBTTagList();

        for (int i = 0; i < maxSize; i++) {
                NBTTagCompound item = new NBTTagCompound();
                item.setInteger(slotKey, i);
                getShadowOfSlot(i).writeToNBT(item);
                shadows.appendTag(item);
        }
        tagcompound.setTag(shadowsKey, shadows);
    }

    public ItemStack getShadowOfSlot(int index) {
        return inventoryShadows.get(index);
    }

    @Override
    public void markDirty() {
        for (int i = 0; i < getSizeInventory(); ++i) {
            if (getStackInSlot(i).getCount() == 0) {
                inventoryContents.set(i, ItemStack.EMPTY);
            }
        }

        for (int i = 0; i < getSizeInventory(); ++i) {
            if (!getStackInSlot(i).isEmpty()) {
                inventoryShadows.set(i, getStackInSlot(i).copy());
            }
        }

        writeToNBT(NBTHelper.getTag(toolbeltItemStack));
    }

    private int getShadowIndex(ItemStack itemStack) {
        for (int i = 0; i < getSizeInventory(); i++) {
            if (itemStack.isItemEqual(getShadowOfSlot(i)) && getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean storeItemInInventory(ItemStack itemStack) {
        // attempt to merge the itemstack with itemstacks in the toolbelt
        for (int i = 0; i < getSizeInventory(); i++) {
            ItemStack storedStack = getStackInSlot(i);
            if (storedStack.isItemEqual(itemStack)
                    && storedStack.getCount() < storedStack.getMaxStackSize()) {

                int moveCount = Math.min(itemStack.getCount(), storedStack.getMaxStackSize() - storedStack.getCount());
                storedStack.grow(moveCount);
                setInventorySlotContents(i, storedStack);
                itemStack.shrink(moveCount);

                if (itemStack.isEmpty()) {
                    return true;
                }
            }
        }

        // attempt to put the itemstack back in a slot it's been in before
        int restockIndex = getShadowIndex(itemStack);
        if (restockIndex != -1) {
            setInventorySlotContents(restockIndex, itemStack);
            return true;
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
}
