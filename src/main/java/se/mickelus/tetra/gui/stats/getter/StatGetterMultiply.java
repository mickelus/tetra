package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;

@ParametersAreNonnullByDefault
public class StatGetterMultiply implements IStatGetter {
    private final IStatGetter[] statGetters;

    private double multiplier;

    public StatGetterMultiply(IStatGetter... statGetters) {
        this(1, statGetters);
    }

    public StatGetterMultiply(double multiplier, IStatGetter... statGetters) {
        this.multiplier = multiplier;
        this.statGetters = statGetters;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return Arrays.stream(statGetters)
                .mapToDouble(getter -> getter.getValue(player, itemStack))
                .reduce(multiplier, (a, b) -> a * b);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return Arrays.stream(statGetters)
                .map(getter -> getter.getValue(player, itemStack, slot))
                .reduce(multiplier, (a, b) -> a * b);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return Arrays.stream(statGetters)
                .map(getter -> getter.getValue(player, itemStack, slot, improvement))
                .reduce(multiplier, (a, b) -> a * b);
    }
}
