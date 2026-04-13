package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class HammerHeadBlockEntity extends BlockEntity {
    private static final String jamKey = "jam";
    public static Supplier<BlockEntityType<HammerHeadBlockEntity>> type;
    private long activationTime = -1;
    private long unjamTime = -1;
    private boolean jammed;

    public HammerHeadBlockEntity(BlockPos p_155268_, BlockState p_155269_) {
        super(type.get(), p_155268_, p_155269_);
    }

    public void activate() {
        activationTime = System.currentTimeMillis();
    }

    public long getActivationTime() {
        return activationTime;
    }

    public long getUnjamTime() {
        return unjamTime;
    }

    public boolean isJammed() {
        return jammed;
    }

    public void setJammed(boolean jammed) {
        this.jammed = jammed;

        if (!jammed) {
            unjamTime = System.currentTimeMillis();
        }

        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        setChanged();
    }


    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);
        this.jammed = compound.contains(jamKey) && compound.getBoolean(jamKey);
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);

        if (isJammed()) {
            compound.putBoolean(jamKey, true);
        }
    }
}
