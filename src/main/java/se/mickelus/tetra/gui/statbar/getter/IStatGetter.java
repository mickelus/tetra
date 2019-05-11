package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IStatGetter {
    public double getValue(EntityPlayer player, ItemStack itemStack);
    public double getValue(EntityPlayer player, ItemStack itemStack, String slot);
    public double getValue(EntityPlayer player, ItemStack itemStack, String slot, String improvement);
}
