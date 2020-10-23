package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.ItemEffect;

public class TooltipGetterBlockingDuration implements ITooltipGetter {
    private IStatGetter durationGetter;

    public TooltipGetterBlockingDuration(IStatGetter durationGetter) {
        this.durationGetter = durationGetter;
    }

    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        if (durationGetter.getValue(player, itemStack) < ItemModularHandheld.blockingDurationLimit) {
            return I18n.format("tetra.stats.blocking_duration.tooltip", durationGetter.getValue(player, itemStack));
        }
        return I18n.format("tetra.stats.blocking.tooltip");
    }

    @Override
    public boolean hasExtendedTooltip(PlayerEntity player, ItemStack itemStack) {
        return true;
    }

    @Override
    public String getTooltipExtension(PlayerEntity player, ItemStack itemStack) {
        return I18n.format("tetra.stats.blocking.tooltip_extended");
    }
}
