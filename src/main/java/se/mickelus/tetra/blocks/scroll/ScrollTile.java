package se.mickelus.tetra.blocks.scroll;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.ArrayUtils;
import se.mickelus.tetra.TetraMod;

import javax.annotation.Nullable;
import java.util.*;

public class ScrollTile extends TileEntity {
    public static final String unlocalizedName = "scroll";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static TileEntityType<ScrollTile> type;

    private static final String scrollsKey = "scrolls";
    private ScrollData[] scrolls = new ScrollData[0];

    public ScrollTile() {
        super(type);
    }

    public ScrollData[] getScrolls() {
        return scrolls;
    }

    public boolean addScroll(ItemStack itemStack) {
        if (scrolls.length < 6) {
            scrolls = ArrayUtils.add(scrolls, ScrollData.read(itemStack));
            setChanged();
            return true;
        }
        return false;
    }

    public ResourceLocation[] getSchematics() {
        boolean isIntricate = isIntricate();
        return Arrays.stream(scrolls)
                .filter(data -> data.isIntricate == isIntricate)
                .map(data -> data.schematics)
                .flatMap(Collection::stream)
                .toArray(ResourceLocation[]::new);
    }

    public ResourceLocation[] getCraftingEffects() {
        boolean isIntricate = isIntricate();
        return Arrays.stream(scrolls)
                .filter(data -> data.isIntricate == isIntricate)
                .map(data -> data.craftingEffects)
                .flatMap(Collection::stream)
                .toArray(ResourceLocation[]::new);
    }

    public boolean isIntricate() {
        return !Arrays.stream(scrolls)
                .anyMatch(data -> !data.isIntricate);
    }


    public CompoundNBT[] getItemTags() {
        return Arrays.stream(scrolls)
                .map(data -> new ScrollData[] { data })
                .map(data -> ScrollData.write(data, new CompoundNBT()))
                .toArray(CompoundNBT[]::new);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return VoxelShapes.block().bounds().move(worldPosition);
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
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        load(getBlockState(), pkt.getTag());
    }

    @Override
    public void load(BlockState blockState, CompoundNBT compound) {
        super.load(blockState, compound);

        scrolls = ScrollData.read(compound);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);

        return ScrollData.write(scrolls, compound);
    }
}
