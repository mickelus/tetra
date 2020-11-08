package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.properties.TetraAttributes;

public class TooltipGetterVelocity implements ITooltipGetter {
    private static final IStatGetter velocityGetter = new StatGetterEffectLevel(ItemEffect.velocity, 1);
    public static final IStatGetter drawStrengthGetter = new StatGetterAttribute(TetraAttributes.drawStrength.get());

    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        double velocityBonus = velocityGetter.getValue(player, itemStack);
        double drawStrength = drawStrengthGetter.getValue(player, itemStack);
        return I18n.format("tetra.stats.velocity.tooltip",
                String.format("%.0f%%", velocityBonus),
                String.format("%.1f",
                        3 * (ModularBowItem.getArrowVelocity(20, drawStrength, (float) velocityBonus / 100f, false)
                                - ModularBowItem.getArrowVelocity(20, drawStrength, 0, false))));
    }
}
