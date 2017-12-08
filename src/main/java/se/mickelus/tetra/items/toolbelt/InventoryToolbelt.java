package se.mickelus.tetra.items.toolbelt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;
import se.mickelus.tetra.NBTHelper;

public class InventoryToolbelt implements IInventory {

    public static int INVENTORY_SIZE = 4;
    
    private ItemStack toolbeltItemStack;

    private NonNullList<ItemStack> inventoryContents;
    private NonNullList<ItemStack> inventoryShadows;

    public InventoryToolbelt(ItemStack stack) {
        toolbeltItemStack = stack;

        inventoryContents = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        inventoryShadows = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        readFromNBT(NBTHelper.getTag(stack));
    }


    public void readFromNBT(NBTTagCompound compound) {
        NBTTagList items = compound.getTagList("ItemInventory", Constants.NBT.TAG_COMPOUND);
        NBTTagList shadows = compound.getTagList("ItemShadows", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound itemTag = items.getCompoundTagAt(i);
            int slot = itemTag.getInteger("Slot");

            if (0 <= slot && slot < getSizeInventory()) {
                inventoryContents.set(slot, new ItemStack(itemTag));
            }
        }

        for (int i = 0; i < shadows.tagCount(); i++) {
            NBTTagCompound item = shadows.getCompoundTagAt(i);
            int slot = item.getInteger("Slot");

            if (0 <= slot && slot < getSizeInventory()) {
                inventoryShadows.set(slot, new ItemStack(item));
            }
        }
    }

    public void writeToNBT(NBTTagCompound tagcompound) {
        NBTTagList items = new NBTTagList();
        NBTTagList shadows = new NBTTagList();

        for (int i = 0; i < getSizeInventory(); i++) {
            if (getStackInSlot(i) != null) {
                NBTTagCompound item = new NBTTagCompound();
                item.setInteger("Slot", i);
                getStackInSlot(i).writeToNBT(item);
                items.appendTag(item);
            }

            if (getShadowOfSlot(i) != null) {
                NBTTagCompound item = new NBTTagCompound();
                item.setInteger("Slot", i);
                getShadowOfSlot(i).writeToNBT(item);
                shadows.appendTag(item);
            }
        }
        // Add the TagList to the ItemStack's Tag Compound with the name "ItemInventory"
        tagcompound.setTag("ItemInventory", items);
        tagcompound.setTag("ItemShadows", shadows);
    }

    @Override
    public int getSizeInventory() {
        return inventoryContents.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryContents.get(index);
    }

    public ItemStack getShadowOfSlot(int index) {
        return inventoryShadows.get(index);
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

        for (int i = 0; i < getSizeInventory(); ++i) {
            if (!getStackInSlot(i).isEmpty()) {
                inventoryShadows.set(i, getStackInSlot(i).copy());
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
    public void closeInventory(EntityPlayer player) {

    }

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
}
