package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

public class StatGetterOr implements IStatGetter {
    IStatGetter[] statGetters;

    public StatGetterOr(IStatGetter... statGetters) {
        this.statGetters = statGetters;
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        return Arrays.stream(statGetters)
                .anyMatch(statGetter -> statGetter.shouldShow(player, currentStack, previewStack));
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return Arrays.stream(statGetters)
                .filter(statGetters -> statGetters.shouldShow(player, itemStack, itemStack))
                .map(statGetter -> statGetter.getValue(player, itemStack))
                .findFirst()
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return Arrays.stream(statGetters)
                .filter(statGetters -> statGetters.shouldShow(player, itemStack, itemStack))
                .map(statGetter -> statGetter.getValue(player, itemStack, slot))
                .findFirst()
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return Arrays.stream(statGetters)
                .filter(statGetters -> statGetters.shouldShow(player, itemStack, itemStack))
                .map(statGetter -> statGetter.getValue(player, itemStack, slot, improvement))
                .findFirst()
                .orElse(0d);
    }
}
