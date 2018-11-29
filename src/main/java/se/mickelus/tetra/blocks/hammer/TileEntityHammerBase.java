package se.mickelus.tetra.blocks.hammer;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;

import javax.annotation.Nullable;

public class TileEntityHammerBase extends TileEntity {

    private static final String slotsKey = "slots";
    private static final String indexKey = "slot";
    private ItemStack[] slots;

    private boolean hasPlateWest = true;
    private boolean hasPlateEast = false;


    public TileEntityHammerBase() {
        slots = new ItemStack[2];
    }

    public boolean isPowered() {
        for (int i = 0; i < slots.length; i++) {
            if (getCellPower(i) <= 0) {
                return false;
            }
        }
        return true;
    }

    public void consumePower() {
        for (ItemStack slot : slots) {
            if (slot != null && (slot.getItem() instanceof ItemCellMagmatic)) {
                ItemCellMagmatic item = (ItemCellMagmatic) slot.getItem();
                item.reduceCharge(slot, 1);
            }
        }
    }

    public boolean hasCellInSlot(int index){
        return index >= 0 && index < slots.length && slots[index] != null;
    }

    public int getCellPower(int index){
        if (index >= 0 && index < slots.length && slots[index] != null) {
            if (slots[index].getItem() instanceof ItemCellMagmatic) {
                ItemCellMagmatic item = (ItemCellMagmatic) slots[index].getItem();
                return item.getCharge(slots[index]);
            }
        }
        return -1;
    }

    public ItemStack removeCellFromSlot(int index) {
        if (index >= 0 && index < slots.length && slots[index] != null) {
            ItemStack itemStack = slots[index];
            slots[index] = null;
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    public boolean putCellInSlot(ItemStack itemStack, int index) {
        if (itemStack.getItem() instanceof ItemCellMagmatic
                && index >= 0 && index < slots.length && slots[index] == null) {
            slots[index] = itemStack;
            return true;
        }
        return false;
    }

    public void removePlate(EnumHammerPlate plate) {
        switch (plate) {
            case EAST:
                hasPlateEast = false;
                break;
            case WEST:
                hasPlateWest = false;
                break;
        }
    }

    public boolean hasPlate(EnumHammerPlate plate) {
        switch (plate) {
            case EAST:
                return hasPlateEast;
            case WEST:
                return hasPlateWest;
        }
        return false;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
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
        if (compound.hasKey(slotsKey)) {
            NBTTagList tagList = compound.getTagList(slotsKey, 10);

            for (int i = 0; i < tagList.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
                int slot = nbttagcompound.getByte(indexKey) & 255;

                if (slot < this.slots.length) {
                    this.slots[slot] = new ItemStack(nbttagcompound);
                }
            }
        }
        hasPlateEast = compound.getBoolean(EnumHammerPlate.EAST.key);
        hasPlateWest = compound.getBoolean(EnumHammerPlate.WEST.key);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.slots.length; ++i) {
            if (this.slots[i] != null) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();

                nbttagcompound.setByte(indexKey, (byte) i);
                this.slots[i].writeToNBT(nbttagcompound);

                nbttaglist.appendTag(nbttagcompound);
            }
        }
        compound.setTag(slotsKey, nbttaglist);

        compound.setBoolean(EnumHammerPlate.EAST.key, hasPlateEast);
        compound.setBoolean(EnumHammerPlate.WEST.key, hasPlateWest);

        return compound;
    }

}
