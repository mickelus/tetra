package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;

public class TooltipGetterExecute implements ITooltipGetter {

    private static final IStatGetter levelGetter = new StatGetterEffectLevel(ItemEffect.execute, 0.1);
    private static final IStatGetter efficiencyGetter = new StatGetterEffectEfficiency(ItemEffect.execute, 1);

    public TooltipGetterExecute() { }

    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        ItemModularHandheld item = CastOptional.cast(itemStack.getItem(), ItemModularHandheld.class).orElse(null);

        double baseDamage = item != null ? item.getAbilityBaseDamage(itemStack) : 0;
        double chargeTime = item != null ? item.getCooldownBase(itemStack) : 0;
        double cooldown = item != null ? item.getCooldownBase(itemStack) : 0;

        return I18n.format("tetra.stats.execute.tooltip",
                String.format("%.1f", baseDamage),
                String.format("%.1f", levelGetter.getValue(player, itemStack)),
                String.format("%.1f", efficiencyGetter.getValue(player, itemStack)),
                String.format("%.1f", chargeTime),
                String.format("%.1f", cooldown));
    }

    @Override
    public boolean hasExtendedTooltip(PlayerEntity player, ItemStack itemStack) {
        return true;
    }

    @Override
    public String getTooltipExtension(PlayerEntity player, ItemStack itemStack) {
        return I18n.format("tetra.stats.execute.tooltip_extended");
    }
}
