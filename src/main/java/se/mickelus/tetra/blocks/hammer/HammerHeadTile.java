package se.mickelus.tetra.blocks.hammer;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;

public class HammerHeadTile extends TileEntity {
    @ObjectHolder(TetraMod.MOD_ID + ":" + HammerHeadBlock.unlocalizedName)
    public static TileEntityType<HammerBaseTile> type;

    private long activationTime = -1;

    public HammerHeadTile() {
        super(type);
    }


    public void activate() {
        activationTime = System.currentTimeMillis();
    }

    public long getActivationTime() {
        return activationTime;
    }

    @Override
    public boolean hasFastRenderer()
    {
        return true;
    }
}
