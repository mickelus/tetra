package se.mickelus.tetra.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import se.mickelus.tetra.effect.potion.SeveredPotionEffect;
import se.mickelus.tetra.effect.potion.StunPotionEffect;

import java.util.Optional;
import java.util.Random;

public class StunEffect {
    public static void perform(ItemStack itemStack, int effectLevel, LivingEntity attacker, LivingEntity target) {
        if (!attacker.level.isClientSide && attacker.getRandom().nextFloat() < effectLevel / 100f) {
            int duration = (int) (EffectHelper.getEffectEfficiency(itemStack, ItemEffect.stun) * 20);

            target.addEffect(new MobEffectInstance(StunPotionEffect.instance, duration, 0, false, false));
            target.getCommandSenderWorld().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_STRONG,
                    SoundSource.PLAYERS, 0.8f, 0.9f);

            ((ServerLevel) target.getCommandSenderWorld()).sendParticles(ParticleTypes.ENTITY_EFFECT, target.getX(), target.getEyeY(), target.getZ(),
                    5, 0, 0, 0, 0);
        }
    }
}
