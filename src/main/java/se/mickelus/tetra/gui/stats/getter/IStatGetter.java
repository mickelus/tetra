package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IStatGetter {

    default public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        double baseValue = getValue(player, ItemStack.EMPTY);
        return getValue(player, currentStack) > baseValue || getValue(player, previewStack) > baseValue;
    }

    public double getValue(Player player, ItemStack itemStack);
    public double getValue(Player player, ItemStack itemStack, String slot);
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement);
}
