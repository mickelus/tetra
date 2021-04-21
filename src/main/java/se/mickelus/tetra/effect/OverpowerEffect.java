package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import se.mickelus.tetra.effect.potion.ExhaustedPotionEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public class OverpowerEffect extends ChargedAbilityEffect {

    public static final OverpowerEffect instance = new OverpowerEffect();

    OverpowerEffect() {
        super(10, 1f, 10, 1, ItemEffect.overpower, TargetRequirement.none, UseAction.SPEAR, "raised");
    }


    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack,
            @Nullable LivingEntity target, @Nullable BlockPos targetPos, @Nullable Vector3d hitVec, int chargedTicks) {
        super.perform(attacker, hand, item, itemStack, target, targetPos, hitVec, chargedTicks);

        boolean isDefensive = isDefensive(item, itemStack, hand);
        int overchargeBonus = canOvercharge(item, itemStack) ? getOverchargeBonus(item, itemStack, chargedTicks) : 0;
        double exhaustDuration = item.getEffectEfficiency(itemStack, ItemEffect.overpower);

        if (!attacker.world.isRemote && !isDefensive) {
            int currentAmp = Optional.ofNullable(attacker.getActivePotionEffect(ExhaustedPotionEffect.instance))
                    .map(EffectInstance::getAmplifier)
                    .orElse(-1);

            int newAmp = currentAmp + 1;

            if (overchargeBonus > 0) {
                currentAmp += (int) (overchargeBonus * item.getEffectEfficiency(itemStack, ItemEffect.abilityOvercharge));
            }

            double comboEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityCombo);
            if (comboEfficiency > 0 && attacker.getEntityWorld().getRandom().nextFloat() < (comboEfficiency * ComboPoints.get(attacker) / 100f)) {
                currentAmp--;

                Random rand = attacker.getEntityWorld().getRandom();
                ((ServerWorld) attacker.getEntityWorld()).spawnParticle(ParticleTypes.HAPPY_VILLAGER,
                        attacker.getPosX(), attacker.getPosY() + attacker.getHeight() / 2, attacker.getPosZ(), 10,
                        rand.nextGaussian() * 0.3, rand.nextGaussian() * attacker.getHeight() * 0.8, rand.nextGaussian() * 0.3, 0.1f);
            }

            if (newAmp > currentAmp) {
                attacker.addPotionEffect(new EffectInstance(ExhaustedPotionEffect.instance, (int) (exhaustDuration * 20), currentAmp, false, true));
            }
        }

        attacker.addExhaustion(0.05f);
        attacker.swing(hand, false);

        int cooldown = getCooldown(item, itemStack);

        if (isDefensive) {
            cooldown = (int) (cooldown * (1 + item.getEffectEfficiency(itemStack, ItemEffect.abilityDefensive) / 100f));
        }

        if (ComboPoints.canSpend(item, itemStack)) {
            ComboPoints.reset(attacker);
        }

        attacker.getCooldownTracker().setCooldown(item, cooldown);
    }

    @Override
    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vector3d hitVec, int chargedTicks) {
        boolean isDefensive = isDefensive(item, itemStack, hand);
        int overchargeBonus = canOvercharge(item, itemStack) ? getOverchargeBonus(item, itemStack, chargedTicks) : 0;

        double damageMultiplier = item.getEffectLevel(itemStack, isDefensive ? ItemEffect.abilityDefensive : ItemEffect.overpower) / 100f;
        double efficiency = item.getEffectEfficiency(itemStack, ItemEffect.overpower);

        if (overchargeBonus > 0) {
            damageMultiplier += overchargeBonus * item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge) / 100d;
        }

        int comboLevel = item.getEffectLevel(itemStack, ItemEffect.abilityCombo);
        if (comboLevel > 0) {
            damageMultiplier += comboLevel * ComboPoints.get(attacker) / 100d;
        }

        AbilityUseResult result = item.hitEntity(itemStack, attacker, target, damageMultiplier, 0.1f, 0.1f);
        if (result != AbilityUseResult.fail) {
            int currentAmplifier = Optional.ofNullable(target.getActivePotionEffect(ExhaustedPotionEffect.instance))
                    .map(EffectInstance::getAmplifier)
                    .orElse(-1);

            int amplifier = currentAmplifier + 2;

            if (isDefensive) {
                amplifier--;
            }

            if (overchargeBonus > 0) {
                amplifier += (int) (overchargeBonus * item.getEffectEfficiency(itemStack, ItemEffect.abilityOvercharge));
            }

            int momentumLevel = item.getEffectLevel(itemStack, ItemEffect.abilityMomentum);
            if (momentumLevel > 0 && currentAmplifier > -1) {
                double momentumEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityMomentum);
                double velocity = momentumLevel / 100d;
                velocity += momentumEfficiency * (currentAmplifier + 1);

                velocity += momentumEfficiency * Optional.ofNullable(attacker.getActivePotionEffect(ExhaustedPotionEffect.instance))
                        .map(EffectInstance::getAmplifier)
                        .map(amp -> amp + 1)
                        .orElse(0);

                velocity *= 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);

                if (velocity > 0) {
                    target.addVelocity(0, velocity, 0);
                }
            }

            target.addPotionEffect(new EffectInstance(ExhaustedPotionEffect.instance, (int) (efficiency * 20), amplifier, false, true));

            target.getEntityWorld().playSound(attacker, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1, 0.8f);
        } else {
            target.getEntityWorld().playSound(attacker, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, SoundCategory.PLAYERS, 1, 0.8f);
        }


        item.tickProgression(attacker, itemStack, result == AbilityUseResult.fail ? 1 : 2);
        item.applyDamage(2, itemStack, attacker);
    }
}
