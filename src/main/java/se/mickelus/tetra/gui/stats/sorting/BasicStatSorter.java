package se.mickelus.tetra.gui.stats.sorting;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.StatFormat;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Comparator;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public class BasicStatSorter implements IStatSorter {
    private final IStatGetter getter;
    private final String name;
    private final StatFormat statFormat;
    private String suffix;
    private boolean inverted;

    public BasicStatSorter(IStatGetter getter, String name, StatFormat statFormat) {
        this.getter = getter;
        this.name = name;
        this.statFormat = statFormat;
    }

    public BasicStatSorter setInverted() {
        inverted = true;
        return this;
    }


    public BasicStatSorter setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    @Override
    public String getName() {
        if (suffix != null) {
            return I18n.get(name) + " " + I18n.get(suffix);
        }
        return I18n.get(name);
    }

    @Override
    public <T> Comparator<T> compare(Player player, Function<? super T, ItemStack> keyExtractor) {
        if (inverted) {
            return Comparator.comparing(a -> getter.getValue(player, keyExtractor.apply(a)));
        }
        return Comparator.comparing(a -> -getter.getValue(player, keyExtractor.apply(a)));
    }

    @Override
    @Nullable
    public String getValue(Player player, ItemStack itemStack) {
        return statFormat.get(getter.getValue(player, itemStack));
    }

    @Override
    public int getWeight(Player player, ItemStack itemStack) {
        return getter.shouldShow(player, itemStack, itemStack) ? 1 : 0;
    }
}
