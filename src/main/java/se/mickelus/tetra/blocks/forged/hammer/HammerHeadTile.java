package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;

import javax.annotation.Nullable;

public class HammerHeadTile extends BlockEntity {
    @ObjectHolder(TetraMod.MOD_ID + ":" + HammerHeadBlock.unlocalizedName)
    public static BlockEntityType<HammerHeadTile> type;

    private long activationTime = -1;
    private long unjamTime = -1;

    private boolean jammed;
    private static final String jamKey = "jam";

    public HammerHeadTile() {
        super(type);
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
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return save(super.getUpdateTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(getBlockState(), pkt.getTag());
    }

    @Override
    public void load(BlockState blockState, CompoundTag compound) {
        super.load(blockState, compound);
        this.jammed = compound.contains(jamKey) && compound.getBoolean(jamKey);
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);

        if (isJammed()) {
            compound.putBoolean(jamKey, true);
        }

        return compound;
    }
}
