package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.ItemEffect;

public class TooltipGetterOverbowed implements ITooltipGetter {
    private static final IStatGetter efficiencyGetter = new StatGetterEffectEfficiency(ItemEffect.overbowed, 100);
    private static final IStatGetter levelGetter = new StatGetterEffectLevel(ItemEffect.overbowed, 1);

    public TooltipGetterOverbowed() { }

    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        return I18n.format("tetra.stats.bow.overbowed.tooltip",
                String.format("%.0f%%", levelGetter.getValue(player, itemStack)),
                String.format("%.0f%%", efficiencyGetter.getValue(player, itemStack)));
    }
}
