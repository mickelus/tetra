package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class TooltipGetterBlockingDuration implements ITooltipGetter {
    private IStatGetter durationGetter;
    private IStatGetter cooldownGetter;

    public TooltipGetterBlockingDuration(IStatGetter durationGetter, IStatGetter cooldownGetter ) {
        this.durationGetter = durationGetter;
        this.cooldownGetter = cooldownGetter;
    }


    @Override
    public String getTooltip(PlayerEntity player, ItemStack itemStack) {
        if (cooldownGetter.getValue(player, itemStack) > 0) {
            return I18n.format("tetra.stats.blocking_duration.tooltip", durationGetter.getValue(player, itemStack));
        }
        return I18n.format("tetra.stats.blocking.tooltip");
    }
}
