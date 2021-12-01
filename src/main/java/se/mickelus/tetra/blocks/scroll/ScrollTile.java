package se.mickelus.tetra.blocks.scroll;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.ArrayUtils;
import se.mickelus.tetra.TetraMod;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collection;
@ParametersAreNonnullByDefault
public class ScrollTile extends BlockEntity {
    public static final String unlocalizedName = "scroll";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static BlockEntityType<ScrollTile> type;

    private static final String scrollsKey = "scrolls";
    private ScrollData[] scrolls = new ScrollData[0];

    public ScrollTile(BlockPos p_155268_, BlockState p_155269_) {
        super(type, p_155268_, p_155269_);
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


    public CompoundTag[] getItemTags() {
        return Arrays.stream(scrolls)
                .map(data -> new ScrollData[] { data })
                .map(data -> ScrollData.write(data, new CompoundTag()))
                .toArray(CompoundTag[]::new);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return Shapes.block().bounds().move(worldPosition);
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(getBlockState(), pkt.getTag());
    }

    @Override
    public void load(BlockState blockState, CompoundTag compound) {
        super.load(blockState, compound);

        scrolls = ScrollData.read(compound);
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);

        return ScrollData.write(scrolls, compound);
    }
}
