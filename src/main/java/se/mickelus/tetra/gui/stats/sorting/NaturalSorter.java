package se.mickelus.tetra.gui.stats.sorting;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Comparator;
import java.util.function.Function;
@ParametersAreNonnullByDefault
public class NaturalSorter implements IStatSorter {
    @Override
    public String getName() {
        return I18n.get("tetra.stats.sorting.none");
    }

    @Override
    public <T> Comparator<T> compare(Player player, Function<? super T, ItemStack> keyExtractor) {
        return (a, b) -> 0;
    }

    @Nullable
    @Override
    public String getValue(Player player, ItemStack itemStack) {
        return null;
    }

    @Override
    public int getWeight(Player player, ItemStack itemStack) {
        return 1;
    }
}
