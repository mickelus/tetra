package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;

public class TooltipGetterHowling implements ITooltipGetter {

    private static final IStatGetter levelGetter = new StatGetterEffectLevel(ItemEffect.howling, 1);
    private static final IStatGetter efficiencyGetter = new StatGetterEffectEfficiency(ItemEffect.howling, 1);

    public TooltipGetterHowling() { }

    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        return I18n.format("tetra.stats.howling.tooltip",
                String.format("%.0f", levelGetter.getValue(player, itemStack)),
                String.format("%.1f", efficiencyGetter.getValue(player, itemStack)));
    }
}
