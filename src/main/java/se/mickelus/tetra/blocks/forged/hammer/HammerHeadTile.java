package se.mickelus.tetra.blocks.forged.hammer;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;

import javax.annotation.Nullable;

public class HammerHeadTile extends TileEntity {
    @ObjectHolder(TetraMod.MOD_ID + ":" + HammerHeadBlock.unlocalizedName)
    public static TileEntityType<HammerHeadTile> type;

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
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return save(super.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(getBlockState(), pkt.getTag());
    }

    @Override
    public void load(BlockState blockState, CompoundNBT compound) {
        super.load(blockState, compound);
        this.jammed = compound.contains(jamKey) && compound.getBoolean(jamKey);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);

        if (isJammed()) {
            compound.putBoolean(jamKey, true);
        }

        return compound;
    }
}
