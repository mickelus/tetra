package se.mickelus.tetra.gui.stats.sorting;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.function.Function;

public interface IStatSorter {
    public String getName();

    public <T> Comparator<T> compare(Player player, Function<? super T, ItemStack> keyExtractor);

    @Nullable
    public String getValue(Player player, ItemStack itemStack);

    public int getWeight(Player player, ItemStack itemStack);
}
