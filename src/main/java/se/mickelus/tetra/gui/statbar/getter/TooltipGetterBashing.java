package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.ItemEffect;

public class TooltipGetterBashing implements ITooltipGetter {

    private static final IStatGetter levelGetter = new StatGetterEffectLevel(ItemEffect.bashing, 1);
    private static final IStatGetter efficiencyGetter = new StatGetterEffectEfficiency(ItemEffect.bashing, 1);

    public TooltipGetterBashing() { }


    @Override
    public String getTooltip(PlayerEntity player, ItemStack itemStack) {
        return I18n.format("tetra.stats.bashing.tooltip",
                String.format("%d", (int) levelGetter.getValue(player, itemStack)),
                String.format("%.1f", efficiencyGetter.getValue(player, itemStack)));
    }
}
