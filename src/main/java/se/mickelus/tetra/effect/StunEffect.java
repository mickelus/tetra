package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import se.mickelus.tetra.effect.potion.SeveredPotionEffect;
import se.mickelus.tetra.effect.potion.StunPotionEffect;

import java.util.Optional;
import java.util.Random;

public class StunEffect {
    public static void perform(ItemStack itemStack, int effectLevel, LivingEntity attacker, LivingEntity target) {
        if (!attacker.world.isRemote && attacker.getRNG().nextFloat() < effectLevel / 100f) {
            int duration = (int) (EffectHelper.getEffectEfficiency(itemStack, ItemEffect.stun) * 20);

            target.addPotionEffect(new EffectInstance(StunPotionEffect.instance, duration, 0, false, false));
            target.getEntityWorld().playSound(null, target.getPosX(), target.getPosY(), target.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG,
                    SoundCategory.PLAYERS, 0.8f, 0.9f);

            ((ServerWorld) target.getEntityWorld()).spawnParticle(ParticleTypes.ENTITY_EFFECT, target.getPosX(), target.getPosYEye(), target.getPosZ(),
                    5, 0, 0, 0, 0);
        }
    }
}
