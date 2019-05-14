package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IStatGetter {

    default public boolean shouldShow(EntityPlayer player, ItemStack currentStack, ItemStack previewStack) {
        double baseValue = getValue(player, ItemStack.EMPTY);
        return getValue(player, currentStack) > baseValue || getValue(player, previewStack) > baseValue;
    }

    public double getValue(EntityPlayer player, ItemStack itemStack);
    public double getValue(EntityPlayer player, ItemStack itemStack, String slot);
    public double getValue(EntityPlayer player, ItemStack itemStack, String slot, String improvement);
}
