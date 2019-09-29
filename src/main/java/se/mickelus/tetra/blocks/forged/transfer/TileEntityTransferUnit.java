package se.mickelus.tetra.blocks.forged.transfer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import org.apache.commons.lang3.EnumUtils;
import se.mickelus.tetra.blocks.IHeatTransfer;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;
import se.mickelus.tetra.util.CastOptional;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Optional;

public class TileEntityTransferUnit extends TileEntity implements ITickable, IHeatTransfer {
    private boolean hasPlate;
    private EnumTransferConfig config;
    private ItemStack cell;

    private boolean isSending = false;
    private boolean isReceiving = false;

    private static final int baseAmount = 8;
    private float efficiency = 1;

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
        notifyBlockUpdate();
    }

    public boolean hasCell() {
        return !cell.isEmpty();
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

    private Optional<IHeatTransfer> getConnectedUnit() {
        return TileEntityOptional.from(world, pos.offset(getFacing()), IHeatTransfer.class);
    }

    @Override
    public int getCharge() {
        return CastOptional.cast(cell.getItem(), ItemCellMagmatic.class)
                .map(item -> item.getCharge(cell))
                .orElse(0);
    }

    @Override
    public float getEfficiency() {
        return hasPlate() ? 1 : 0.9f;
    }

    @Override
    public boolean canRecieve() {
        return getEffectPowered().equals(EnumTransferEffect.RECEIVE)
                && hasCell()
                && getCharge() < ItemCellMagmatic.maxCharge;
    }

    @Override
    public boolean canSend() {
        return getEffectPowered().equals(EnumTransferEffect.SEND)
                && hasCell()
                && getCharge() > 0;
    }

    @Override
    public void setReceiving(boolean receiving) {
        isReceiving = receiving;

        if (isReceiving) {
            setSending(false);
        }

        notifyBlockUpdate();
    }

    @Override
    public boolean isReceiving() {
        return isReceiving;
    }

    @Override
    public void setSending(boolean sending) {
        isSending = sending;

        if (isSending) {
            isReceiving = false;
        }

        notifyBlockUpdate();
    }

    @Override
    public boolean isSending() {
        return isSending;
    }

    @Override
    public int getReceiveLimit() {
        return baseAmount;
    }

    @Override
    public int getSendLimit() {
        return baseAmount;
    }

    @Override
    public int drain(int amount) {
        return CastOptional.cast(cell.getItem(), ItemCellMagmatic.class)
                .map(item -> {
                    int drained = item.drainCharge(cell, amount);

                    if (item.getCharge(cell) == 0) {
                        runDrainedEffects();
                    }

                    return drained;
                })
                .orElse(0);
    }

    @Override
    public int fill(int amount) {
        return CastOptional.cast(cell.getItem(), ItemCellMagmatic.class)
                .map(item -> {
                    if (item.getCharge(cell) == 0) {
                        notifyBlockUpdate();
                    }
                    int overfill = item.recharge(cell, amount);

                    if (item.getCharge(cell) == ItemCellMagmatic.maxCharge) {
                        runFilledEffects();
                    }

                    return overfill;
                })
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
                            int overfill = connected.fill((int) (amount * efficiency));

                            if (overfill > 0) {
                                fill(overfill);
                            }

                            markDirty();
                        } else {
                            setSending(false);
                            connected.setReceiving(false);
                        }
                    });
        } else {
            getConnectedUnit().ifPresent(connected -> connected.setReceiving(false));
            setSending(false);
        }
    }

    private void runDrainedEffects() {
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                    pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                    10,  0, 0, 0, 0.02f);
            world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS,
                    0.2f, 1);
        }
    }

    private void runFilledEffects() {
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.FLAME,
                    pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                    5,  0, 0, 0, 0.02f);
            world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS,
                    0.2f, 1);
        }
    }

    @Override
    public void updateTransferState() {
        switch (getEffectPowered()) {
            case SEND:
                getConnectedUnit().ifPresent(connected -> {
                    boolean canTransfer = canSend() && connected.canRecieve();
                    isSending = canTransfer;
                    isReceiving = false;
                    connected.setReceiving(canTransfer);

                    efficiency = getEfficiency() * connected.getEfficiency();
                });
                break;
            case RECEIVE:
                getConnectedUnit().ifPresent(connected -> {
                    if (isSending()) {
                        setSending(false);
                    }

                    if (connected.canSend()) {
                        connected.updateTransferState();
                    }
                });
                break;
            case REDSTONE:
                getConnectedUnit().ifPresent(connected -> {
                    connected.setSending(false);
                    connected.setReceiving(false);
                    setSending(false);
                    setReceiving(false);
                });
                break;
        }
        markDirty();
    }

    public void notifyBlockUpdate() {
        markDirty();
        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state,3);
    }

    public EnumFacing getFacing() {
        return world.getBlockState(pos).getValue(BlockTransferUnit.propFacing);
    }

    @Override
    public void readFromNBT(CompoundNBT compound) {
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

    public static final void writeCell(CompoundNBT compound, ItemStack cell) {
        if (!cell.isEmpty()) {
            CompoundNBT cellNBT = new CompoundNBT();
            cell.writeToNBT(cellNBT);
            compound.setTag("cell", cellNBT);
        }
    }

    public static void writePlate(CompoundNBT compound, boolean hasPlate) {
        compound.setBoolean("plate", hasPlate);
    }

    public static void writeConfig(CompoundNBT compound, EnumTransferConfig config) {
        compound.setString(EnumTransferConfig.prop.getName(), config.toString());
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT compound) {
        super.writeToNBT(compound);

        writeCell(compound, cell);

        writeConfig(compound, config);

        writePlate(compound, hasPlate);

        return compound;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return writeToNBT(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        this.readFromNBT(packet.getNbtCompound());
        IBlockState state = world.getBlockState(pos);

        updateTransferState();
        world.notifyBlockUpdate(pos, state, state,3);
    }
}
