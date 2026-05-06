package se.mickelus.tetra.effect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.effect.potion.ExhaustedPotionEffect;

import java.util.Optional;

public class RavenousEffect {
    private static final int exhaustionInitialDuration = 100;
    private static final int exhaustionStackDuration = 20;

    public static void perform(LivingEntity entity, ItemStack itemStack, double multiplier) {
        if (!entity.level().isClientSide && entity instanceof Player player) {
            double effectProbability = EffectHelper.getEffectLevel(itemStack, ItemEffect.ravenous);

            if (effectProbability > 0 && entity.getRandom().nextDouble() < effectProbability / 100 * multiplier) {
                float exhaustAmount = EffectHelper.getEffectEfficiency(itemStack, ItemEffect.ravenous);
                player.causeFoodExhaustion(exhaustAmount);
            }

            if (effectProbability > 0 && player.getFoodData().getFoodLevel() == 0) {
                var effect = EffectHelper.effectHolder(ExhaustedPotionEffect.instance);
                MobEffectInstance currentExhaustedInstance = player.getEffect(effect);
                int currentAmplifier = Optional.ofNullable(currentExhaustedInstance)
                        .map(MobEffectInstance::getAmplifier)
                        .orElse(-1);

                int duration = Optional.ofNullable(currentExhaustedInstance)
                        .map(MobEffectInstance::getDuration)
                        .map(currentDuration -> currentDuration + exhaustionStackDuration)
                        .orElse(exhaustionInitialDuration);
                player.addEffect(new MobEffectInstance(effect, duration, currentAmplifier + 1, false, false, true));
            }
        }
    }
}
