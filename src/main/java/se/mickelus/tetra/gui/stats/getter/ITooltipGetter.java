package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import se.mickelus.tetra.Tooltips;

public interface ITooltipGetter {
    public default String getTooltip(Player player, ItemStack itemStack) {
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
    public default String getTooltipExtended(Player player, ItemStack itemStack) {
        if (hasExtendedTooltip(player, itemStack)) {
            return getTooltipBase(player, itemStack) + "\n \n" + Tooltips.expanded.getString() + "\n"
                    + ChatFormatting.GRAY + getTooltipExtension(player, itemStack);
        }

        return getTooltip(player, itemStack);
    }

    public String getTooltipBase(Player player, ItemStack itemStack);

    public default boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return false;
    }

    public default String getTooltipExtension(Player player, ItemStack itemStack) {
        return null;
    }
}
