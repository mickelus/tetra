package se.mickelus.tetra.gui.impl.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.ItemEffect;

public class TooltipGetterCriticalStrike implements ITooltipGetter {
    private static final IStatGetter efficiencyGetter = new StatGetterEffectEfficiency(ItemEffect.criticalStrike, 100);
    private static final IStatGetter levelGetter = new StatGetterEffectLevel(ItemEffect.criticalStrike, 1);

    public TooltipGetterCriticalStrike() { }

    @Override
    public String getTooltip(PlayerEntity player, ItemStack itemStack) {
        return I18n.format("stats.criticalStrike.tooltip",
                String.format("%.0f%%", levelGetter.getValue(player, itemStack)),
                String.format("%.0f%%", efficiencyGetter.getValue(player, itemStack)),
                String.format("%.0f%%", levelGetter.getValue(player, itemStack)));
    }
}
