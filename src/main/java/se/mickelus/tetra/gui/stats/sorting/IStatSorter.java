package se.mickelus.tetra.gui.stats.sorting;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.function.Function;

public interface IStatSorter {
    String getName();

    <T> Comparator<T> compare(Player player, Function<? super T, ItemStack> keyExtractor);

    @Nullable
    String getValue(Player player, ItemStack itemStack);

    int getWeight(Player player, ItemStack itemStack);
}
