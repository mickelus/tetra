package se.mickelus.tetra.items.toolbelt.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;
import se.mickelus.tetra.NBTHelper;

public class InventoryToolbelt implements IInventory {
    protected static final String slotKey = "slot";

    protected ItemStack toolbeltItemStack;

    protected final String inventoryKey;
    protected NonNullList<ItemStack> inventoryContents;
    protected int numSlots = 0;
    protected int maxSize = 0;

    public InventoryToolbelt(String inventoryKey, ItemStack stack, int maxSize) {
        this.inventoryKey = inventoryKey;
        toolbeltItemStack = stack;

        this.maxSize = maxSize;
        inventoryContents = NonNullList.withSize(maxSize, ItemStack.EMPTY);
    }


    public void readFromNBT(NBTTagCompound compound) {
        NBTTagList items = compound.getTagList(inventoryKey, Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound itemTag = items.getCompoundTagAt(i);
            int slot = itemTag.getInteger(slotKey);

            if (0 <= slot && slot < maxSize) {
                inventoryContents.set(slot, new ItemStack(itemTag));
            }
        }
    }

    public void writeToNBT(NBTTagCompound tagcompound) {
        NBTTagList items = new NBTTagList();

        for (int i = 0; i < maxSize; i++) {
            if (getStackInSlot(i) != null) {
                NBTTagCompound item = new NBTTagCompound();
                item.setInteger(slotKey, i);
                getStackInSlot(i).writeToNBT(item);
                items.appendTag(item);
            }
        }

        tagcompound.setTag(inventoryKey, items);
    }

    @Override
    public int getSizeInventory() {
        return numSlots;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getSizeInventory(); i++) {
            if (!getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryContents.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventoryContents, index, count);

        if (!itemstack.isEmpty()) {
            this.markDirty();
        }

        return itemstack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack itemStack = this.inventoryContents.get(index);

        if (itemStack.isEmpty()) {
            return itemStack;
        } else {
            this.inventoryContents.set(index, ItemStack.EMPTY);
            return itemStack;
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.inventoryContents.set(index, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        for (int i = 0; i < getSizeInventory(); ++i) {
            if (getStackInSlot(i).getCount() == 0) {
                inventoryContents.set(i, ItemStack.EMPTY);
            }
        }

        writeToNBT(NBTHelper.getTag(toolbeltItemStack));
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        inventoryContents.clear();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return null;
    }

    public ItemStack takeItemStack(int index) {
        ItemStack itemStack = getStackInSlot(index);
        setInventorySlotContents(index, ItemStack.EMPTY);
        return itemStack;
    }

    public void emptyOverflowSlots(EntityPlayer player) {
        for (int i = getSizeInventory(); i < maxSize; i++) {
            moveStackToPlayer(removeStackFromSlot(i), player);
        }
        this.markDirty();
    }

    protected void moveStackToPlayer(ItemStack itemStack, EntityPlayer player) {
        if (!itemStack.isEmpty()) {
            if (!player.inventory.addItemStackToInventory(itemStack)) {
                player.dropItem(itemStack, false);
            }
        }
    }

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
