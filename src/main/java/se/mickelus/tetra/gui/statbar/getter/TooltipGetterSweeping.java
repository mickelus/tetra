package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;

public class TooltipGetterSweeping implements ITooltipGetter {

    private static IStatGetter levelGetter;
    private static final IStatGetter efficiencyGetter = new StatGetterEffectEfficiency(ItemEffect.sweeping, 1, 1);

    public TooltipGetterSweeping(IStatGetter levelGetter) {
        this.levelGetter = levelGetter;
    }

    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        return I18n.format("tetra.stats.sweeping.tooltip",
                String.format("%.1f", efficiencyGetter.getValue(player, itemStack)),
                String.format("%.1f%%", levelGetter.getValue(player, itemStack)));
    }
}
