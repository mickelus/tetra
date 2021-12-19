package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TooltipGetterCriticalStrike implements ITooltipGetter {
    private static final IStatGetter efficiencyGetter = new StatGetterEffectEfficiency(ItemEffect.criticalStrike, 100);
    private static final IStatGetter levelGetter = new StatGetterEffectLevel(ItemEffect.criticalStrike, 1);

    public TooltipGetterCriticalStrike() {
    }

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        String level = String.format("%.0f%%", levelGetter.getValue(player, itemStack));
        String efficiency = String.format("%.0f%%", efficiencyGetter.getValue(player, itemStack));

        return I18n.get("tetra.stats.criticalStrike.tooltip", level, efficiency, level, level, efficiency);
    }

    @Override
    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return true;
    }

    @Override
    public String getTooltipExtension(Player player, ItemStack itemStack) {
        return I18n.get("tetra.stats.criticalStrike.tooltip_extended");
    }
}
