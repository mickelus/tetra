package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class StatGetterAdd implements IStatGetter {
    private final IStatGetter[] statGetters;
    private double offset = 0;

    public StatGetterAdd(IStatGetter... statGetters) {
        this.statGetters = statGetters;
    }

    public StatGetterAdd(double offset, IStatGetter... statGetters) {
        this(statGetters);
        this.offset = offset;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return Optional.of(Arrays.stream(statGetters)
                        .mapToDouble(getter -> getter.getValue(player, itemStack))
                        .sum())
                .map(value -> value != 0 ? value + offset : value)
                .get();
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return Optional.of(Arrays.stream(statGetters)
                        .mapToDouble(getter -> getter.getValue(player, itemStack, slot))
                        .sum())
                .map(value -> value != 0 ? value + offset : value)
                .get();
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return Optional.of(Arrays.stream(statGetters)
                        .mapToDouble(getter -> getter.getValue(player, itemStack, slot, improvement))
                        .sum())
                .map(value -> value != 0 ? value + offset : value)
                .get();
    }
}
