package se.mickelus.tetra.gui.stats.sorting;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.stats.getter.IStatGetter;
import se.mickelus.tetra.gui.stats.getter.StatFormat;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.function.Function;

public class BasicStatSorter implements IStatSorter {
    private IStatGetter getter;
    private String name;
    private String suffix;
    private StatFormat statFormat;
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
            return I18n.format(name) + " " + I18n.format(suffix);
        }
        return I18n.format(name);
    }

    @Override
    public <T> Comparator<T> compare(PlayerEntity player, Function<? super T, ItemStack> keyExtractor) {
        if (inverted) {
            return Comparator.comparing(a -> getter.getValue(player, keyExtractor.apply(a)));
        }
        return Comparator.comparing(a -> -getter.getValue(player, keyExtractor.apply(a)));
    }

    @Override
    @Nullable
    public String getValue(PlayerEntity player, ItemStack itemStack) {
        return statFormat.get(getter.getValue(player, itemStack));
    }

    @Override
    public int getWeight(PlayerEntity player, ItemStack itemStack) {
        return getter.shouldShow(player, itemStack, itemStack) ? 1 : 0;
    }
}
