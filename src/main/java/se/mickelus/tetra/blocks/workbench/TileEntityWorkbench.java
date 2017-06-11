package se.mickelus.tetra.blocks.workbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import se.mickelus.tetra.network.PacketPipeline;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityWorkbench extends TileEntity implements IInventory {

    private static final String STACKS_KEY = "stacks";
    private static final String SLOT_KEY = "slot";
    private static final String STATE_KEY = "state";

    private NonNullList<ItemStack> stacks;

    public static final int EMPTY_STATE = 0;
    public static final int READY_STATE = 1;
    public static final int PRE_UPGRADE_STATE = 2;
    public static final int SALVAGE_STATE = 3;
    public static final int SALVAGED_STATE = 4;

    private int currentState = 0;


    public TileEntityWorkbench() {
        stacks = NonNullList.withSize(3, ItemStack.EMPTY);
    }

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int state) {
        currentState = state;

        sync();
    }

    private void sync() {
        if (world.isRemote) {
            PacketPipeline.instance.sendToServer(new UpdateWorkbenchPacket(pos, getCurrentState()));
        } else {
            world.notifyBlockUpdate(pos, getBlockType().getDefaultState(), getBlockType().getDefaultState(), 3);
            markDirty();
        }
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        this.readFromNBT(packet.getNbtCompound());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey(STACKS_KEY)) {
            NBTTagList tagList = compound.getTagList(STACKS_KEY, 10);

            for (int i = 0; i < tagList.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
                int slot = nbttagcompound.getByte(SLOT_KEY) & 255;

                if (slot >= 0 && slot < this.stacks.size()) {
                    this.stacks.set(slot, new ItemStack(nbttagcompound));
                }
            }
        }

        currentState = compound.getInteger(STATE_KEY);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.stacks.size(); ++i) {
            if (this.stacks.get(i) != null) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();

                nbttagcompound.setByte(SLOT_KEY, (byte)i);
                this.stacks.get(i).writeToNBT(nbttagcompound);

                nbttaglist.appendTag(nbttagcompound);
            }
        }

        compound.setTag(STACKS_KEY, nbttaglist);

        compound.setInteger(STATE_KEY, currentState);

        return compound;
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }


    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.stacks) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
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
            this.markDirty();
        }

        return itemstack;
    }

    @Nullable
    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.stacks, index);
    }

    @Override
    public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
        this.stacks.set(index, stack);

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
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if(index == 0) {
            return true;
        }

        return false;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

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
        return "workbench";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }
}
