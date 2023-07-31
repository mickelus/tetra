package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TooltipGetterSweepingFocus implements ITooltipGetter {
    private final IStatGetter statGetter;

    public TooltipGetterSweepingFocus(IStatGetter statGetter) {
        this.statGetter = statGetter;
    }

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        double focus = statGetter.getValue(player, itemStack);

        if (focus > 1) {
            return I18n.get("tetra.stats.tool.sweepingFocus_width.tooltip", String.format("%.1f", focus));
        } else if (focus < 1) {
            return I18n.get("tetra.stats.tool.sweepingFocus_depth.tooltip", String.format("%.1f", 2 - focus));
        }
        return I18n.get("tetra.stats.tool.sweepingFocus.tooltip");

    }
}
