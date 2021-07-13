package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import se.mickelus.tetra.Tooltips;

public interface ITooltipGetter {
    public default String getTooltip(PlayerEntity player, ItemStack itemStack) {
        if (hasExtendedTooltip(player, itemStack)) {
            return getTooltipBase(player, itemStack) + "\n \n" + Tooltips.expand.getString();
        }

        return getTooltipBase(player, itemStack);
    }

    /**
     * Used for showing extended tooltips when shift is held down
     * @param player
     * @param itemStack
     * @return
     */
    public default String getTooltipExtended(PlayerEntity player, ItemStack itemStack) {
        if (hasExtendedTooltip(player, itemStack)) {
            return getTooltipBase(player, itemStack) + "\n \n" + Tooltips.expanded.getString() + "\n"
                    + TextFormatting.GRAY + getTooltipExtension(player, itemStack);
        }

        return getTooltip(player, itemStack);
    }

    public String getTooltipBase(PlayerEntity player, ItemStack itemStack);

    public default boolean hasExtendedTooltip(PlayerEntity player, ItemStack itemStack) {
        return false;
    }

    public default String getTooltipExtension(PlayerEntity player, ItemStack itemStack) {
        return null;
    }
}
