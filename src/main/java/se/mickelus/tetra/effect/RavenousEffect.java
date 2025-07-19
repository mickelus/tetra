package se.mickelus.tetra.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RavenousEffect {
    public static void perform(LivingEntity entity, ItemStack itemStack, double multiplier) {
        if (!entity.level().isClientSide && entity instanceof Player player) {
            double effectProbability = EffectHelper.getEffectLevel(itemStack, ItemEffect.ravenous);
            if (effectProbability > 0 && entity.getRandom().nextDouble() < effectProbability / 100 * multiplier) {
                float exhaustAmount = EffectHelper.getEffectEfficiency(itemStack, ItemEffect.ravenous);
                player.getFoodData().addExhaustion(exhaustAmount);
            }
        }
    }
}
