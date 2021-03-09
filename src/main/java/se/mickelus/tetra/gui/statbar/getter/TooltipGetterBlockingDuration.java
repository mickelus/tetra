package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.util.CastOptional;

public class  TooltipGetterBlockingDuration implements ITooltipGetter {
    private IStatGetter durationGetter;
    private IStatGetter cooldownGetter;

    public TooltipGetterBlockingDuration(IStatGetter durationGetter, IStatGetter cooldownGetter) {
        this.durationGetter = durationGetter;
        this.cooldownGetter = cooldownGetter;
    }

    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        double cooldownMultiplier = cooldownGetter.getValue(player, itemStack);
        if (cooldownMultiplier > 0) {
            if (cooldownMultiplier != 1) {
                double baseCooldown = CastOptional.cast(itemStack.getItem(), ItemModularHandheld.class)
                        .map(item -> item.getCooldownBase(itemStack))
                        .orElse(1d);
                return I18n.format("tetra.stats.blocking_duration_cooldown.tooltip",
                        String.format("%.1f", durationGetter.getValue(player, itemStack)),
                        String.format("%.2f", cooldownMultiplier),
                        String.format("%.1f", cooldownMultiplier * baseCooldown));
            }
            return I18n.format("tetra.stats.blocking_duration.tooltip", String.format("%.1f", durationGetter.getValue(player, itemStack)));
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
