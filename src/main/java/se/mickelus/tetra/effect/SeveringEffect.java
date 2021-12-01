package se.mickelus.tetra.effect;

import com.mojang.math.Vector3f;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.effect.potion.SeveredPotionEffect;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.Random;
@ParametersAreNonnullByDefault
public class SeveringEffect {
    public static void perform(ItemStack itemStack, int effectLevel, LivingEntity attacker, LivingEntity target) {
        if (attacker.getRandom().nextFloat() < effectLevel / 100f) {
            int stackCap = (int) EffectHelper.getEffectEfficiency(itemStack, ItemEffect.severing) - 1;

            int currentAmplifier = Optional.ofNullable(target.getEffect(SeveredPotionEffect.instance))
                    .map(MobEffectInstance::getAmplifier)
                    .orElse(-1);

            target.addEffect(new MobEffectInstance(SeveredPotionEffect.instance, 1200, Math.min(currentAmplifier + 1, stackCap), false, false));

            if (!target.level.isClientSide) {
                Random rand = target.getRandom();
                target.getCommandSenderWorld().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_STRONG,
                        SoundSource.PLAYERS, 0.8f, 0.9f);
                ((ServerLevel) target.getCommandSenderWorld()).sendParticles(new DustParticleOptions(new Vector3f(0.5f, 0, 0), 0.5f),
                        target.getX() + target.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                        target.getY() + target.getBbHeight() * (0.2 + rand.nextGaussian() * 0.4),
                        target.getZ() + target.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                        20,
                        0, 0, 0, 0f);
            }
        }
    }
}
