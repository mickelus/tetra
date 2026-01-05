package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

public class StatGetterAnd implements IStatGetter {
    IStatGetter[] statGetters;

    public StatGetterAnd(IStatGetter... statGetters) {
        this.statGetters = statGetters;
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        return Arrays.stream(statGetters)
                .allMatch(statGetter -> statGetter.shouldShow(player, currentStack, previewStack));
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return shouldShow(player, itemStack, itemStack) && statGetters.length > 0
                ? statGetters[0].getValue(player, itemStack)
                : 0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return shouldShow(player, itemStack, itemStack) && statGetters.length > 0
                ? statGetters[0].getValue(player, itemStack, slot)
                : 0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return shouldShow(player, itemStack, itemStack) && statGetters.length > 0
                ? statGetters[0].getValue(player, itemStack, slot, improvement)
                : 0;
    }
}
