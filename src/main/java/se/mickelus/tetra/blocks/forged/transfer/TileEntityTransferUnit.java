package se.mickelus.tetra.blocks.forged.transfer;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.EnumUtils;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;

public class TileEntityTransferUnit extends TileEntity {
    private boolean hasPlate;
    private EnumTransferConfig config;
    private ItemStack cell;

    public TileEntityTransferUnit() {
        hasPlate = true;
        config = EnumTransferConfig.A;
        cell = ItemStack.EMPTY;
    }

    public EnumFacing getFacing() {
        return world.getBlockState(pos).getValue(BlockTransferUnit.propFacing);
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

        if (compound.hasKey("cell")) {
            cell = new ItemStack(compound.getCompoundTag("cell"));
        } else {
            cell = ItemStack.EMPTY;
        }

        config = EnumTransferConfig.A;
        if (compound.hasKey(EnumTransferConfig.prop.getName())) {
            String enumName = compound.getString(EnumTransferConfig.prop.getName());
            if (EnumUtils.isValidEnum(EnumTransferConfig.class, enumName)) {
                config = EnumTransferConfig.valueOf(enumName);
            }
        }

        hasPlate = compound.getBoolean("plate");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        if (!cell.isEmpty()) {
            NBTTagCompound cellNBT = new NBTTagCompound();
            cell.writeToNBT(cellNBT);
            compound.setTag("cell", cellNBT);
        }

        compound.setString(EnumTransferConfig.prop.getName(), config.toString());

        compound.setBoolean("plate", hasPlate);

        return compound;
    }

    public void removePlate() {
        hasPlate = false;
        markDirty();
    }

    public void attachPlate() {
        hasPlate = true;
        markDirty();
    }

    public boolean hasPlate() {
        return hasPlate;
    }

    public EnumTransferConfig getConfiguration() {
        return config;
    }

    public void reconfigure() {
        config = EnumTransferConfig.getNextConfiguration(config);
        markDirty();
    }

    public boolean hasCell() {
        return !cell.isEmpty();
    }

    public int getCellFuel() {
        return CastOptional.cast(cell.getItem(), ItemCellMagmatic.class)
                .map(item -> item.getCharge(cell))
                .orElse(0);
    }

    public ItemStack removeCell() {
        ItemStack removedCell = cell;
        cell = ItemStack.EMPTY;

        return removedCell;
    }

    public boolean putCell(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemCellMagmatic) {
            cell = itemStack;
            return true;
        }
        return false;
    }
}
