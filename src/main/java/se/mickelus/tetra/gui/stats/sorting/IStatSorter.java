package se.mickelus.tetra.gui.stats.sorting;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.module.Priority;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.function.Function;

public interface IStatSorter {
    String getName();

    <T> Comparator<T> compare(Player player, Function<? super T, ItemStack> keyExtractor);

    @Nullable
    String getValue(Player player, ItemStack itemStack);

    boolean shouldShow(Player player, ItemStack itemStack);

    default Priority getPriority() {
        return Priority.BASE;
    }
}
