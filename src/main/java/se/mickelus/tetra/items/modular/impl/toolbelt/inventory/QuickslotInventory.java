package se.mickelus.tetra.items.modular.impl.toolbelt.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.impl.toolbelt.ModularToolbeltItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.SlotType;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class QuickslotInventory extends ToolbeltInventory {

    public static final int maxSize = 12;
    private static final String inventoryKey = "quickInventory";
    private static final String shadowsKey = "quickShadows";


    private NonNullList<ItemStack> inventoryShadows;

    public QuickslotInventory(ItemStack stack) {
        super(inventoryKey, stack, maxSize, SlotType.quick);
        ModularToolbeltItem item = (ModularToolbeltItem) stack.getItem();
        numSlots = item.getNumSlots(stack, SlotType.quick);

        inventoryShadows = NonNullList.withSize(maxSize, ItemStack.EMPTY);

        readFromNBT(stack.getOrCreateTag());
    }


    @Override
    public void readFromNBT(CompoundTag tagCompound) {
        super.readFromNBT(tagCompound);
        ListTag shadows = tagCompound.getList(shadowsKey, Tag.TAG_COMPOUND);

        for (int i = 0; i < shadows.size(); i++) {
            CompoundTag item = shadows.getCompound(i);
            int slot = item.getInt(slotKey);

            if (0 <= slot && slot < getContainerSize()) {
                inventoryShadows.set(slot, ItemStack.of(item));
            }
        }
    }

    public void writeToNBT(CompoundTag tagcompound) {
        super.writeToNBT(tagcompound);
        ListTag shadows = new ListTag();

        for (int i = 0; i < maxSize; i++) {
                CompoundTag item = new CompoundTag();
                item.putInt(slotKey, i);
                getShadowOfSlot(i).save(item);
                shadows.add(item);
        }
        tagcompound.put(shadowsKey, shadows);
    }

    public ItemStack getShadowOfSlot(int index) {
        return inventoryShadows.get(index);
    }

    @Override
    public void setChanged() {
        for (int i = 0; i < getContainerSize(); ++i) {
            if (getItem(i).getCount() == 0) {
                inventoryContents.set(i, ItemStack.EMPTY);
            }
        }

        for (int i = 0; i < getContainerSize(); ++i) {
            if (!getItem(i).isEmpty()) {
                inventoryShadows.set(i, getItem(i).copy());
            }
        }

        writeToNBT(toolbeltItemStack.getOrCreateTag());
    }

    private int getShadowIndex(ItemStack itemStack) {
        for (int i = 0; i < getContainerSize(); i++) {
            if (itemStack.sameItem(getShadowOfSlot(i)) && getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean storeItemInInventory(ItemStack itemStack) {
        if (!isItemValid(itemStack)) {
            return false;
        }

        // attempt to merge the itemstack with itemstacks in the toolbelt
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack storedStack = getItem(i);
            if (storedStack.sameItem(itemStack)
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

        // attempt to put the itemstack back in a slot it's been in before
        int restockIndex = getShadowIndex(itemStack);
        if (restockIndex != -1) {
            setItem(restockIndex, itemStack);
            return true;
        }

        // put item in the first empty slot
        for (int i = 0; i < getContainerSize(); i++) {
            if (getItem(i).isEmpty()) {
                setItem(i, itemStack);
                return true;
            }
        }
        return false;
    }
}
