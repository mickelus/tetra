package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
@ParametersAreNonnullByDefault
public class StatGetterMultiply implements IStatGetter {
    private IStatGetter[] statGetters;

    public StatGetterMultiply(IStatGetter ... statGetters) {
        this.statGetters = statGetters;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return Arrays.stream(statGetters)
                .mapToDouble(getter -> getter.getValue(player, itemStack))
                .reduce(1d, (a, b) -> a * b);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return Arrays.stream(statGetters)
                .map(getter -> getter.getValue(player, itemStack, slot))
                .reduce(1d, (a, b) -> a * b);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return Arrays.stream(statGetters)
                .map(getter -> getter.getValue(player, itemStack, slot, improvement))
                .reduce(1d, (a, b) -> a * b);
    }
}
