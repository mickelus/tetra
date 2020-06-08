package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface ITooltipGetter {
    public String getTooltip(PlayerEntity player, ItemStack itemStack);

    /**
     * Used for showing extended tooltips when shift is held down
     * @param player
     * @param itemStack
     * @return
     */
    public default String getTooltipExtended(PlayerEntity player, ItemStack itemStack) {
        return getTooltip(player, itemStack);
    }
}
