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
        if (attacker.getRNG().nextFloat() < effectLevel / 100f) {
            int stackCap = (int) EffectHelper.getEffectEfficiency(itemStack, ItemEffect.severing) - 1;

            int currentAmplifier = Optional.ofNullable(target.getActivePotionEffect(SeveredPotionEffect.instance))
                    .map(EffectInstance::getAmplifier)
                    .orElse(-1);

            target.addPotionEffect(new EffectInstance(SeveredPotionEffect.instance, 1200, Math.min(currentAmplifier + 1, stackCap), false, false));

            if (!target.world.isRemote) {
                Random rand = target.getRNG();
                target.getEntityWorld().playSound(null, target.getPosX(), target.getPosY(), target.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG,
                        SoundCategory.PLAYERS, 0.8f, 0.9f);
                ((ServerWorld) target.getEntityWorld()).spawnParticle(new RedstoneParticleData(0.5f, 0, 0, 0.5f),
                        target.getPosX() + target.getWidth() * (0.3 + rand.nextGaussian() * 0.4),
                        target.getPosY() + target.getHeight() * (0.2 + rand.nextGaussian() * 0.4),
                        target.getPosZ() + target.getWidth() * (0.3 + rand.nextGaussian() * 0.4),
                        20,
                        0, 0, 0, 0f);
            }
        }
    }
}
