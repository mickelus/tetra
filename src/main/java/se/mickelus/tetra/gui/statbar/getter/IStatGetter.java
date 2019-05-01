package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.item.ItemStack;

public interface IStatGetter {
    public double getValue(ItemStack itemStack, String slot, String improvement);
}
