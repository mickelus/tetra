package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TooltipGetterBlockingDuration implements ITooltipGetter {
    private final IStatGetter durationGetter;
    private final IStatGetter cooldownGetter;

    public TooltipGetterBlockingDuration(IStatGetter durationGetter, IStatGetter cooldownGetter) {
        this.durationGetter = durationGetter;
        this.cooldownGetter = cooldownGetter;
    }

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        double cooldownMultiplier = cooldownGetter.getValue(player, itemStack);
        if (cooldownMultiplier > 0) {
            if (cooldownMultiplier != 1) {
                double baseCooldown = CastOptional.cast(itemStack.getItem(), ItemModularHandheld.class)
                        .map(item -> item.getCooldownBase(itemStack))
                        .orElse(1d);
                return I18n.get("tetra.stats.blocking_duration_cooldown.tooltip",
                        String.format("%.1f", durationGetter.getValue(player, itemStack)),
                        String.format("%.2f", cooldownMultiplier),
                        String.format("%.1f", cooldownMultiplier * baseCooldown));
            }
            return I18n.get("tetra.stats.blocking_duration.tooltip", String.format("%.1f", durationGetter.getValue(player, itemStack)));
        }
        return I18n.get("tetra.stats.blocking.tooltip");
    }

    @Override
    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return true;
    }

    @Override
    public String getTooltipExtension(Player player, ItemStack itemStack) {
        return I18n.get("tetra.stats.blocking.tooltip_extended");
    }
}
