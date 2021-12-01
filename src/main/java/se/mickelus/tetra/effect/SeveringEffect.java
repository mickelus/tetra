package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import se.mickelus.tetra.effect.potion.SeveredPotionEffect;

import java.util.Optional;
import java.util.Random;

public class SeveringEffect {
    public static void perform(ItemStack itemStack, int effectLevel, LivingEntity attacker, LivingEntity target) {
        if (attacker.getRandom().nextFloat() < effectLevel / 100f) {
            int stackCap = (int) EffectHelper.getEffectEfficiency(itemStack, ItemEffect.severing) - 1;

            int currentAmplifier = Optional.ofNullable(target.getEffect(SeveredPotionEffect.instance))
                    .map(EffectInstance::getAmplifier)
                    .orElse(-1);

            target.addEffect(new EffectInstance(SeveredPotionEffect.instance, 1200, Math.min(currentAmplifier + 1, stackCap), false, false));

            if (!target.level.isClientSide) {
                Random rand = target.getRandom();
                target.getCommandSenderWorld().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_STRONG,
                        SoundCategory.PLAYERS, 0.8f, 0.9f);
                ((ServerWorld) target.getCommandSenderWorld()).sendParticles(new RedstoneParticleData(0.5f, 0, 0, 0.5f),
                        target.getX() + target.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                        target.getY() + target.getBbHeight() * (0.2 + rand.nextGaussian() * 0.4),
                        target.getZ() + target.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                        20,
                        0, 0, 0, 0f);
            }
        }
    }
}
