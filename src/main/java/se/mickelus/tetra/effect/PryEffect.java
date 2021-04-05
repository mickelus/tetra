package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import se.mickelus.tetra.effect.potion.PriedPotionEffect;
import se.mickelus.tetra.effect.potion.SeveredPotionEffect;
import se.mickelus.tetra.effect.potion.StunPotionEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.ParticleHelper;

import java.util.Optional;

public class PryEffect {
    public static final double flatCooldown = 2;
    public static final double cooldownSpeedMultiplier = 3;

    public static void perform( PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, int effectLevel, LivingEntity target) {
        int currentAmplifier = Optional.ofNullable(target.getActivePotionEffect(PriedPotionEffect.instance))
                .map(EffectInstance::getAmplifier)
                .orElse(-1);

        target.addPotionEffect(new EffectInstance(PriedPotionEffect.instance, (int) (item.getEffectEfficiency(itemStack, ItemEffect.pry) * 20),
                currentAmplifier + effectLevel, false, false));

        item.hitEntity(itemStack, attacker, target, 0.5, 0.2f, 0.2f);

        target.getEntityWorld().playSound(attacker, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, SoundCategory.PLAYERS, 0.8f, 0.8f);

        if (!target.getEntityWorld().isRemote) {
            ParticleHelper.spawnArmorParticles((ServerWorld) target.getEntityWorld(), target);
        }

        attacker.addExhaustion(0.05f);
        attacker.swing(hand, false);
        attacker.getCooldownTracker().setCooldown(item, (int) (flatCooldown + item.getCooldownBase(itemStack) * cooldownSpeedMultiplier) * 20);

        item.tickProgression(attacker, itemStack, 2);
        item.applyDamage(2, itemStack, attacker);
    }
}
