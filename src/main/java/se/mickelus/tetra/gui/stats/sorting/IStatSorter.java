package se.mickelus.tetra.gui.stats.sorting;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.function.Function;

public interface IStatSorter {
    public String getName();

    public <T> Comparator<T> compare(PlayerEntity player, Function<? super T, ItemStack> keyExtractor);

    @Nullable
    public String getValue(PlayerEntity player, ItemStack itemStack);

    public int getWeight(PlayerEntity player, ItemStack itemStack);
}
