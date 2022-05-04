package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class HammerHeadTile extends BlockEntity {
    private static final String jamKey = "jam";
    @ObjectHolder(TetraMod.MOD_ID + ":" + HammerHeadBlock.identifier)
    public static BlockEntityType<HammerHeadTile> type;
    private long activationTime = -1;
    private long unjamTime = -1;
    private boolean jammed;

    public HammerHeadTile(BlockPos p_155268_, BlockState p_155269_) {
        super(type, p_155268_, p_155269_);
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
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.jammed = compound.contains(jamKey) && compound.getBoolean(jamKey);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        if (isJammed()) {
            compound.putBoolean(jamKey, true);
        }
    }
}
