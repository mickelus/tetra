package se.mickelus.tetra.gui.stats.sorting;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.function.Function;

public class NaturalSorter implements IStatSorter {
    @Override
    public String getName() {
        return I18n.format("tetra.stats.sorting.none");
    }

    @Override
    public <T> Comparator<T> compare(PlayerEntity player, Function<? super T, ItemStack> keyExtractor) {
        return (a, b) -> 0;
    }

    @Nullable
    @Override
    public String getValue(PlayerEntity player, ItemStack itemStack) {
        return null;
    }

    @Override
    public int getWeight(PlayerEntity player, ItemStack itemStack) {
        return 1;
    }
}
