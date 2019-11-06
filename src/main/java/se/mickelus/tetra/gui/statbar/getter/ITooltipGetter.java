package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface ITooltipGetter {
    public String getTooltip(PlayerEntity player, ItemStack itemStack);
}
