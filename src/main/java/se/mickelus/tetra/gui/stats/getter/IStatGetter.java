package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IStatGetter {

    default boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        double baseValue = getValue(player, ItemStack.EMPTY);
        return getValue(player, currentStack) > baseValue || getValue(player, previewStack) > baseValue;
    }

    double getValue(Player player, ItemStack itemStack);

    double getValue(Player player, ItemStack itemStack, String slot);

    double getValue(Player player, ItemStack itemStack, String slot, String improvement);
}
