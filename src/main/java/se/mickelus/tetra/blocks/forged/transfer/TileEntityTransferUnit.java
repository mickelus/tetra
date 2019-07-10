package se.mickelus.tetra.blocks.forged.transfer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import org.apache.commons.lang3.EnumUtils;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;
import se.mickelus.tetra.util.CastOptional;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Optional;

public class TileEntityTransferUnit extends TileEntity implements ITickable {
    private boolean hasPlate;
    private EnumTransferConfig config;
    private ItemStack cell;

    private boolean isSending = false;
    private boolean isReceiving = false;

    private static final int baseAmount = 8;
    private int leakAmount = 0;

    public TileEntityTransferUnit() {
        hasPlate = true;
        config = EnumTransferConfig.A;
        cell = ItemStack.EMPTY;
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

    public EnumTransferEffect getEffect() {
        return EnumTransferEffect.fromConfig(config, 0);
    }

    /**
     * Returns the effect with redstone power taken into consideration, if the block is powered with redstone the
     * REDSTONE effect will be translated into SEND/RECEIVE depending on the powered side.
     * @return the effect with redstone power taken into consideration
     */
    public EnumTransferEffect getEffectPowered() {
        EnumTransferEffect effect = EnumTransferEffect.fromConfig(config, 0);
        if (effect.equals(EnumTransferEffect.REDSTONE)) {
            EnumFacing facing = getFacing();

            if (world.isSidePowered(pos.offset(facing.rotateY()), facing.rotateY())) {
                return EnumTransferEffect.SEND;
            }

            if (world.isSidePowered(pos.offset(facing.rotateYCCW()), facing.rotateYCCW())) {
                return EnumTransferEffect.RECEIVE;
            }
        }
        return effect;
    }

    public void reconfigure() {
        config = EnumTransferConfig.getNextConfiguration(config);
        updateTransferState();
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

        updateTransferState();
        return removedCell;
    }

    public boolean putCell(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemCellMagmatic) {
            cell = itemStack;

            updateTransferState();
            return true;
        }
        return false;
    }

    private Optional<TileEntityTransferUnit> getConnectedUnit() {
        return TileEntityOptional.from(world, pos.offset(getFacing()), TileEntityTransferUnit.class);
    }

    public boolean canRecieve() {
        return getEffectPowered().equals(EnumTransferEffect.RECEIVE)
                && hasCell()
                && getCellFuel() < ItemCellMagmatic.maxCharge;
    }

    public boolean canSend() {
        return getEffectPowered().equals(EnumTransferEffect.SEND)
                && hasCell()
                && getCellFuel() > 0;
    }

    public boolean isReceiving() {
        return isReceiving;
    }

    public boolean isSending() {
        return isSending;
    }

    private int drain(int amount) {
        return CastOptional.cast(cell.getItem(), ItemCellMagmatic.class)
                .map(item -> item.drainCharge(cell, amount))
                .orElse(0);
    }

    private int fill(int amount) {
        return CastOptional.cast(cell.getItem(), ItemCellMagmatic.class)
                .map(item -> item.recharge(cell, amount))
                .orElse(0);
    }

    @Override
    public void update() {
        if (isSending) {
            if (world.getTotalWorldTime() % 5 == 0) {
                transfer();
            }
        }
    }

    public void transfer() {
        if (canSend()) {
            getConnectedUnit()
                    .ifPresent(connected -> {
                        if (connected.canRecieve()) {
                            int amount = drain(baseAmount);
                            int connectedCurrent = connected.getCellFuel();
                            int overfill = connected.fill(amount - leakAmount);

                            if (overfill > 0) {
                                fill(overfill);
                            }

                            markDirty();

                            // triggers visual update from empty to charged cell
                            if (connectedCurrent == 0) {
                                notifyBlockUpdate();
                            } else {
                                markDirty();
                            }
                        } else {
                            isSending = false;
                            connected.isReceiving = false;

                            runEndEffects();

                            notifyBlockUpdate();
                        }
                    });
        } else {
            getConnectedUnit().ifPresent(connected -> connected.isReceiving = false);
            isSending = false;

            runEndEffects();

            notifyBlockUpdate();
        }
    }

    private void runEndEffects() {
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                    pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                    10,  0, 0, 0, 0.02f);
            world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS,
                    0.2f, 1);
        }
    }

    public void updateTransferState() {
        switch (getEffectPowered()) {
            case SEND:
                getConnectedUnit().ifPresent(connected -> {
                    boolean canTransfer = canSend() && connected.canRecieve();
                    isSending = canTransfer;
                    isReceiving = false;
                    connected.isReceiving = canTransfer;
                    connected.isSending = false;

                    leakAmount = 0;
                    if (!hasPlate()) {
                        leakAmount++;
                    }
                    if (!connected.hasPlate()) {
                        leakAmount++;
                    }

                    connected.notifyBlockUpdate();
                });
                break;
            case RECEIVE:
                getConnectedUnit().ifPresent(connected -> {
                    boolean canTransfer = canRecieve() && connected.canSend();
                    connected.isSending = canTransfer;
                    connected.isReceiving = false;
                    isReceiving = canTransfer;
                    isSending = false;

                    connected.notifyBlockUpdate();
                });
                break;
            case REDSTONE:
                getConnectedUnit().ifPresent(connected -> {
                    connected.isSending = false;
                    connected.isReceiving = false;
                    isReceiving = false;
                    isSending = false;

                    connected.notifyBlockUpdate();
                });
                break;
        }
        markDirty();
    }

    private void notifyBlockUpdate() {
        markDirty();
        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state,3);
    }

    public EnumFacing getFacing() {
        return world.getBlockState(pos).getValue(BlockTransferUnit.propFacing);
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
        IBlockState state = world.getBlockState(pos);

        updateTransferState();
        world.notifyBlockUpdate(pos, state, state,3);
    }
}
