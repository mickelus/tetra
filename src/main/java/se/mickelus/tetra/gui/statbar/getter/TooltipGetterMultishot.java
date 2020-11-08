package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;

public class TooltipGetterMultishot implements ITooltipGetter {
    private static final IStatGetter efficiencyGetter = new StatGetterEffectEfficiency(ItemEffect.multishot, 1);
    private static final IStatGetter levelGetter = new StatGetterEffectLevel(ItemEffect.multishot, 1);

    public TooltipGetterMultishot() { }

    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        return I18n.format("tetra.stats.multishot.tooltip",
                String.format("%.0f", levelGetter.getValue(player, itemStack)),
                String.format("%.1f", efficiencyGetter.getValue(player, itemStack)));
    }
}
