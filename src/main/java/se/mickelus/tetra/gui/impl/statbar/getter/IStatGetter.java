package se.mickelus.tetra.gui.impl.statbar.getter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface IStatGetter {

    default public boolean shouldShow(PlayerEntity player, ItemStack currentStack, ItemStack previewStack) {
        double baseValue = getValue(player, ItemStack.EMPTY);
        return getValue(player, currentStack) > baseValue || getValue(player, previewStack) > baseValue;
    }

    public double getValue(PlayerEntity player, ItemStack itemStack);
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot);
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement);
}
