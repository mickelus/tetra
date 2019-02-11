package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import se.mickelus.tetra.NBTHelper;

import javax.annotation.Nullable;

public class TileEntityForgedContainer extends TileEntity implements IInventory {

    private NonNullList<ItemStack> stacks;

    public TileEntityForgedContainer() {
        stacks = NonNullList.withSize(16, ItemStack.EMPTY);
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        this.readFromNBT(packet.getNbtCompound());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        NBTHelper.readItemStacks(compound, stacks);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        NBTHelper.writeItemStacks(stacks, compound);

        return compound;
    }


    @Override
    public int getSizeInventory() {
        return stacks.size();
    }


    @Override
    public boolean isEmpty() {
        return stacks.stream()
                .allMatch(ItemStack::isEmpty);
    }

    @Nullable
    @Override
    public ItemStack getStackInSlot(int index) {
        return this.stacks.get(index);
    }

    @Nullable
    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.stacks, index, count);

        if (!itemstack.isEmpty()) {
            markDirty();
        }

        return itemstack;
    }

    @Nullable
    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack itemstack = ItemStackHelper.getAndRemove(this.stacks, index);

        if (!itemstack.isEmpty()) {
            markDirty();
        }

        return itemstack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack itemStack) {
        stacks.set(index, itemStack);

        if (!itemStack.isEmpty() && itemStack.getCount() > getInventoryStackLimit()) {
            itemStack.setCount(getInventoryStackLimit());
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) { }

    @Override
    public void closeInventory(EntityPlayer player) { }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) { }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.stacks.clear();
    }

    @Override
    public String getName() {
        return BlockForgedContainer.unlocalizedName;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }


}
