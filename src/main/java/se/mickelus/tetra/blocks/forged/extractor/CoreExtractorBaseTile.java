package se.mickelus.tetra.blocks.forged.extractor;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.IHeatTransfer;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import java.util.Optional;

public class CoreExtractorBaseTile extends TileEntity implements ITickableTileEntity, IHeatTransfer {
    @ObjectHolder(TetraMod.MOD_ID + ":" + CoreExtractorBaseBlock.unlocalizedName)
    public static TileEntityType<CoreExtractorBaseTile> type;

    private boolean isSending = false;

    private static final int sendLimit = 4;

    private static final String chargeKey = "charge";
    private static final int maxCharge = 128;
    private static final int drainAmount = 4;
    private int currentCharge = 0;
    private float efficiency;

    public CoreExtractorBaseTile() {
        super(type);
    }

    public boolean canRefill() {
        return getPiston().isPresent() && CoreExtractorPipeBlock.isPowered(level, worldPosition.below());
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
    public void tick() {
        if (!level.isClientSide) {
            if (isSending) {
                if (level.getGameTime() % 5 == 0) {
                    transfer();
                }
            } else if (currentCharge > 0) {
                if (level.getGameTime() % 20 == 0) {
                    currentCharge = Math.max(0, currentCharge - drainAmount);
                }
            }
        }
    }

    @Override
    public void updateTransferState() {
        getConnectedUnit().ifPresent(connected -> {
            boolean canSend = currentCharge > 0;
            boolean canRecieve = connected.canRecieve();

            setSending(canSend && canRecieve);
            connected.setReceiving(canSend && canRecieve);

            efficiency = getEfficiency() * connected.getEfficiency();

            if (!canSend && canRecieve && canRefill()) {
                getPiston().ifPresent(CoreExtractorPistonTile::activate);
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

                            setChanged();
                        } else {

                            setSending(false);
                            connected.setReceiving(false);

                            notifyBlockUpdate();
                        }

                        if (canRefill()) {
                            getPiston().ifPresent(CoreExtractorPistonTile::activate);
                        }
                    } else {
                        setSending(false);
                        connected.setReceiving(false);

                        notifyBlockUpdate();
                    }
                });

    }

    private void notifyBlockUpdate() {
        setChanged();
        BlockState state = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, state, state,3);
    }

    public Direction getFacing() {
        return getBlockState().getValue(CoreExtractorBaseBlock.facingProp);
    }

    private Optional<IHeatTransfer> getConnectedUnit() {
        return TileEntityOptional.from(level, worldPosition.relative(getFacing()), IHeatTransfer.class);
    }

    private Optional<CoreExtractorPistonTile> getPiston() {
        return TileEntityOptional.from(level, worldPosition.relative(Direction.UP), CoreExtractorPistonTile.class);
    }

    @Override
    public void load(BlockState blockState, CompoundNBT compound) {
        super.load(blockState, compound);

        if (compound.contains(chargeKey)) {
            currentCharge = compound.getInt(chargeKey);
        } else {
            currentCharge = 0;
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);

        compound.putInt(chargeKey, currentCharge);

        return compound;
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(worldPosition, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        this.load(getBlockState(), packet.getTag());
//        BlockState state = getBlockState();

//        world.notifyBlockUpdate(pos, state, state,3);
    }
}
