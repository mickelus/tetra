package se.mickelus.tetra.blocks.forged.extractor;

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
import se.mickelus.tetra.blocks.forged.transfer.EnumTransferConfig;
import se.mickelus.tetra.blocks.forged.transfer.TileEntityTransferUnit;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Optional;

public class TileEntityCoreExtractorBase extends TileEntity implements ITickable {

    private boolean isSending = false;

    private static final int sendLimit = 16;

    private static final int maxCharge = 128;
    private int currentCharge = 0;
    private int leakAmount;

    public TileEntityCoreExtractorBase() {
    }

    public boolean canRecieve() {
        return false;
    }

    public boolean canSend() {
        return currentCharge > 0;
    }

    public boolean canRefill() {
        return TileEntityOptional.from(world, pos.offset(EnumFacing.UP), TileEntityCoreExtractorPiston.class)
                .map(te -> !te.isActive())
                .orElse(false);
    }

    public boolean isReceiving() {
        return false;
    }

    public boolean isSending() {
        return isSending;
    }

    public int getSendLimit() {
        return sendLimit;
    }

    public int drain(int amount) {
        if (amount > currentCharge) {
            int drained = currentCharge;
            currentCharge = 0;
            return drained;
        }

        currentCharge -= amount;
        return amount;
    }

    public int fill(int amount) {
        if (amount + currentCharge > maxCharge) {
            int overfill = amount + currentCharge - maxCharge;
            currentCharge = maxCharge;
            return overfill;
        }

        currentCharge += amount;

        updateTransferState();

        return 0;
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
        getConnectedUnit()
                .ifPresent(connected -> {
                    if (connected.canRecieve()) {
                        if (canSend()) {
                            int amount = drain(Math.min(getSendLimit(), connected.getReceiveLimit()));
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
                            if (canRefill()) {
                                TileEntityOptional.from(world, pos.offset(EnumFacing.UP), TileEntityCoreExtractorPiston.class)
                                        .ifPresent(TileEntityCoreExtractorPiston::activate);
                            }

                            isSending = false;
                            connected.setReceiving(false);

                            runFilledEffects();

                            notifyBlockUpdate();
                        }
                    } else {
                        isSending = false;
                        connected.setReceiving(false);

                        runFilledEffects();

                        notifyBlockUpdate();
                    }
                });

    }

    private void runFilledEffects() {
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                    pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                    10,  0, 0, 0, 0.02f);
            world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS,
                    0.2f, 1);
        }
    }

    public void updateTransferState() {
        getConnectedUnit().ifPresent(connected -> {
            boolean canTransfer = canSend() && connected.canRecieve();
            isSending = canTransfer;
            connected.setReceiving(canTransfer);

            leakAmount = connected.hasPlate() ? 0 : 1;

            connected.notifyBlockUpdate();
        });
        markDirty();
    }

    private void notifyBlockUpdate() {
        markDirty();
        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state,3);
    }

    public EnumFacing getFacing() {
        return world.getBlockState(pos).getValue(BlockCoreExtractorBase.propFacing);
    }

    private Optional<TileEntityTransferUnit> getConnectedUnit() {
        return TileEntityOptional.from(world, pos.offset(getFacing()), TileEntityTransferUnit.class);
    }

    private Optional<TileEntityCoreExtractorPiston> getPiston() {
        return TileEntityOptional.from(world, pos.offset(EnumFacing.UP), TileEntityCoreExtractorPiston.class);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (compound.hasKey("charge")) {
            currentCharge = compound.getInteger("charge");
        } else {
            currentCharge = 0;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setInteger("charge", currentCharge);

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
