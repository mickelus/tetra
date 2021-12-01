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
        if (!attacker.level.isClientSide && attacker.getRandom().nextFloat() < effectLevel / 100f) {
            int duration = (int) (EffectHelper.getEffectEfficiency(itemStack, ItemEffect.stun) * 20);

            target.addEffect(new EffectInstance(StunPotionEffect.instance, duration, 0, false, false));
            target.getCommandSenderWorld().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_STRONG,
                    SoundCategory.PLAYERS, 0.8f, 0.9f);

            ((ServerWorld) target.getCommandSenderWorld()).sendParticles(ParticleTypes.ENTITY_EFFECT, target.getX(), target.getEyeY(), target.getZ(),
                    5, 0, 0, 0, 0);
        }
    }
}
