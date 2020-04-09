package se.mickelus.tetra.items.modular.impl.toolbelt.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.items.modular.impl.toolbelt.ModularToolbeltItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.SlotType;

public class InventoryQuickslot extends InventoryToolbelt {

    public static final int maxSize = 12;
    private static final String inventoryKey = "quickInventory";
    private static final String shadowsKey = "quickShadows";


    private NonNullList<ItemStack> inventoryShadows;

    public InventoryQuickslot(ItemStack stack) {
        super(inventoryKey, stack, maxSize, SlotType.quick);
        ModularToolbeltItem item = (ModularToolbeltItem) stack.getItem();
        numSlots = item.getNumSlots(stack, SlotType.quick);

        inventoryShadows = NonNullList.withSize(maxSize, ItemStack.EMPTY);

        readFromNBT(NBTHelper.getTag(stack));
    }


    @Override
    public void readFromNBT(CompoundNBT tagCompound) {
        super.readFromNBT(tagCompound);
        ListNBT shadows = tagCompound.getList(shadowsKey, Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < shadows.size(); i++) {
            CompoundNBT item = shadows.getCompound(i);
            int slot = item.getInt(slotKey);

            if (0 <= slot && slot < getSizeInventory()) {
                inventoryShadows.set(slot, ItemStack.read(item));
            }
        }
    }

    public void writeToNBT(CompoundNBT tagcompound) {
        super.writeToNBT(tagcompound);
        ListNBT shadows = new ListNBT();

        for (int i = 0; i < maxSize; i++) {
                CompoundNBT item = new CompoundNBT();
                item.putInt(slotKey, i);
                getShadowOfSlot(i).write(item);
                shadows.add(item);
        }
        tagcompound.put(shadowsKey, shadows);
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
