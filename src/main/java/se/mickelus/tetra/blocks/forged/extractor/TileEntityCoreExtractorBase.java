package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import se.mickelus.tetra.blocks.IHeatTransfer;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Optional;

public class TileEntityCoreExtractorBase extends TileEntity implements ITickable, IHeatTransfer {

    private boolean isSending = false;

    private static final int sendLimit = 4;

    private static final String chargeKey = "charge";
    private static final int maxCharge = 128;
    private static final int drainAmount = 4;
    private int currentCharge = 0;
    private float efficiency;

    public TileEntityCoreExtractorBase() {
    }

    public boolean canRefill() {
        return getPiston().isPresent() && BlockCoreExtractorPipe.isPowered(world, pos.down());
    }

    @Override
    public void setReceiving(boolean receiving) {
        if (receiving) {
            isSending = false;
        }

        notifyBlockUpdate();
    }

    @Override
    public boolean isReceiving() {
        return false;
    }

    @Override
    public boolean canRecieve() {
        return false;
    }

    @Override
    public void setSending(boolean sending) {
        isSending = sending;

        notifyBlockUpdate();
    }

    @Override
    public boolean isSending() {
        return isSending;
    }

    @Override
    public boolean canSend() {
        return currentCharge > 0 || canRefill();
    }

    @Override
    public int getReceiveLimit() {
        return 0;
    }

    @Override
    public int getSendLimit() {
        return sendLimit;
    }

    @Override
    public int drain(int amount) {
        if (amount > currentCharge) {
            int drained = currentCharge;
            currentCharge = 0;
            return drained;
        }

        currentCharge -= amount;
        return amount;
    }

    @Override
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
    public int getCharge() {
        return currentCharge;
    }

    @Override
    public float getEfficiency() {
        return 1;
    }

    @Override
    public void update() {
        if (isSending) {
            if (world.getTotalWorldTime() % 5 == 0) {
                transfer();
            }
        } else if (currentCharge > 0) {
            if (world.getTotalWorldTime() % 20 == 0) {
                currentCharge = Math.max(0, currentCharge - drainAmount);
            }
        }
    }

    @Override
    public void updateTransferState() {
        getConnectedUnit().ifPresent(connected -> {
            boolean canSend = canSend();
            boolean canRecieve = connected.canRecieve();

            setSending(canSend && canRecieve);
            connected.setReceiving(canSend && canRecieve);

            efficiency = getEfficiency() * connected.getEfficiency();

            if (!canSend && canRecieve && canRefill()) {
                getPiston().ifPresent(TileEntityCoreExtractorPiston::activate);
            }
        });
    }


    public void transfer() {
        getConnectedUnit()
                .ifPresent(connected -> {
                    if (connected.canRecieve()) {
                        if (currentCharge > 0) {
                            int amount = drain(Math.min(getSendLimit(), connected.getReceiveLimit()));
                            int overfill = connected.fill((int) (amount * efficiency));

                            if (overfill > 0) {
                                fill(overfill);
                            }

                            markDirty();
                        } else {

                            setSending(false);
                            connected.setReceiving(false);

                            notifyBlockUpdate();
                        }

                        if (canRefill()) {
                            getPiston().ifPresent(TileEntityCoreExtractorPiston::activate);
                        }
                    } else {
                        setSending(false);
                        connected.setReceiving(false);

                        notifyBlockUpdate();
                    }
                });

    }

    private void notifyBlockUpdate() {
        markDirty();
        IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state,3);
    }

    public EnumFacing getFacing() {
        return world.getBlockState(pos).getValue(BlockCoreExtractorBase.propFacing);
    }

    private Optional<IHeatTransfer> getConnectedUnit() {
        return TileEntityOptional.from(world, pos.offset(getFacing()), IHeatTransfer.class);
    }

    private Optional<TileEntityCoreExtractorPiston> getPiston() {
        return TileEntityOptional.from(world, pos.offset(EnumFacing.UP), TileEntityCoreExtractorPiston.class);
    }

    @Override
    public void readFromNBT(CompoundNBT compound) {
        super.readFromNBT(compound);

        if (compound.hasKey(chargeKey)) {
            currentCharge = compound.getInteger(chargeKey);
        } else {
            currentCharge = 0;
        }
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT compound) {
        super.writeToNBT(compound);

        compound.setInteger(chargeKey, currentCharge);

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
