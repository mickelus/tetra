package se.mickelus.tetra.blocks.forged.transfer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.IHeatTransfer;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;
import se.mickelus.tetra.util.CastOptional;
import se.mickelus.tetra.util.ITetraTicker;
import se.mickelus.tetra.util.TileEntityOptional;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
@ParametersAreNonnullByDefault
public class TransferUnitTile extends BlockEntity implements ITetraTicker, IHeatTransfer {
    @ObjectHolder(TetraMod.MOD_ID + ":" + TransferUnitBlock.unlocalizedName)
    public static BlockEntityType<TransferUnitTile> type;

    private ItemStack cell;

    private static final int baseAmount = 8;
    private float efficiency = 1;

    public TransferUnitTile(BlockPos p_155268_, BlockState p_155269_) {
        super(type, p_155268_, p_155269_);
        cell = ItemStack.EMPTY;
    }

    public boolean canRecieve() {
        return TransferUnitBlock.getEffectPowered(level, worldPosition, getBlockState()).equals(EnumTransferEffect.receive)
                && hasCell()
                && getCharge() < ItemCellMagmatic.maxCharge;
    }

    public boolean canSend() {
        return TransferUnitBlock.getEffectPowered(level, worldPosition, getBlockState()).equals(EnumTransferEffect.send)
                && hasCell()
                && getCharge() > 0;
    }

    @Override
    public void setReceiving(boolean receiving) {
        TransferUnitBlock.setReceiving(level, worldPosition, getBlockState(), receiving);
    }

    @Override
    public boolean isReceiving() {
        return TransferUnitBlock.isReceiving(getBlockState());
    }

    @Override
    public void setSending(boolean sending) {
        TransferUnitBlock.setSending(level, worldPosition, getBlockState(), sending);
    }

    @Override
    public boolean isSending() {
        return TransferUnitBlock.isSending(getBlockState());
    }

    public boolean hasCell() {
        return !cell.isEmpty();
    }

    public ItemStack removeCell() {
        ItemStack removedCell = cell;
        cell = ItemStack.EMPTY;

        TransferUnitBlock.updateCellProp(level, worldPosition, hasCell(), getCharge());
        updateTransferState();
        return removedCell;
    }

    public ItemStack getCell() {
        return cell;
    }

    public boolean putCell(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemCellMagmatic) {
            cell = itemStack;

            TransferUnitBlock.updateCellProp(level, worldPosition, hasCell(), getCharge());
            updateTransferState();
            return true;
        }
        return false;
    }

    private Optional<IHeatTransfer> getConnectedUnit() {
        return TileEntityOptional.from(level, worldPosition.relative(TransferUnitBlock.getFacing(getBlockState())), IHeatTransfer.class);
    }

    @Override
    public int getCharge() {
        return CastOptional.cast(cell.getItem(), ItemCellMagmatic.class)
                .map(item -> item.getCharge(cell))
                .orElse(0);
    }

    @Override
    public float getEfficiency() {
        return TransferUnitBlock.hasPlate(getBlockState()) ? 1 : 0.9f;
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
                        TransferUnitBlock.updateCellProp(level, worldPosition, hasCell(), 0);
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
                    int initialCharge = item.getCharge(cell);

                    int overfill = item.recharge(cell, amount);

                    if (item.getCharge(cell) == ItemCellMagmatic.maxCharge) {
                        runFilledEffects();
                    }

                    if (initialCharge == 0) {
                        TransferUnitBlock.updateCellProp(level, worldPosition, hasCell(), getCharge());
                    }

                    setChanged();
                    return overfill;
                })
                .orElse(0);
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState blockState) {
        if (!level.isClientSide
                && level.getGameTime() % 5 == 0
                && TransferUnitBlock.isSending(blockState)) {
            transfer();
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

                            setChanged();
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
        if (level instanceof ServerLevel) {
            ((ServerLevel) level).sendParticles(ParticleTypes.SMOKE,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.7, worldPosition.getZ() + 0.5,
                    10,  0, 0, 0, 0.02f);
            level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                    0.2f, 1);
        }
    }

    private void runFilledEffects() {
        if (level instanceof ServerLevel) {
            ((ServerLevel) level).sendParticles(ParticleTypes.FLAME,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.7, worldPosition.getZ() + 0.5,
                    5,  0, 0, 0, 0.02f);
            level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                    0.2f, 1);
        }
    }

    @Override
    public void updateTransferState() {
        switch (TransferUnitBlock.getEffectPowered(level, worldPosition, getBlockState())) {
            case send:
                getConnectedUnit().ifPresent(connected -> {
                    boolean canTransfer = canSend() && connected.canRecieve();
                    setReceiving(false);
                    setSending(canTransfer);
                    connected.setReceiving(canTransfer);

                    efficiency = getEfficiency() * connected.getEfficiency();
                });
                break;
            case receive:
                getConnectedUnit().ifPresent(connected -> {
                    if (isSending()) {
                        setSending(false);
                    }

                    if (connected.canSend()) {
                        connected.updateTransferState();
                    }
                });
                break;
            case redstone:
                getConnectedUnit().ifPresent(connected -> {
                    connected.setSending(false);
                    connected.setReceiving(false);
                    setSending(false);
                    setReceiving(false);
                });
                break;
        }
        setChanged();
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        if (compound.contains("cell")) {
            cell = ItemStack.of(compound.getCompound("cell"));
        } else {
            cell = ItemStack.EMPTY;
        }
    }

    public static final void writeCell(CompoundTag compound, ItemStack cell) {
        if (!cell.isEmpty()) {
            CompoundTag cellNBT = new CompoundTag();
            cell.save(cellNBT);
            compound.put("cell", cellNBT);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);

        writeCell(compound, cell);

        return compound;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        this.load(packet.getTag());
    }
}
