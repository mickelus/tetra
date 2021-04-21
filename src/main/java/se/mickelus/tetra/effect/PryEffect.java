package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import se.mickelus.tetra.effect.potion.*;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.ParticleHelper;

import java.util.Optional;
import java.util.Random;

public class PryEffect {
    public static final double flatCooldown = 2;
    public static final double cooldownSpeedMultiplier = 3;

    public static final double damageMultiplier = 0.5;

    private static int getCooldown(ItemModularHandheld item, ItemStack itemStack) {
        float speedBonus = (100 - item.getEffectLevel(itemStack, ItemEffect.abilitySpeed)) / 100f;
        return (int) ((flatCooldown + item.getCooldownBase(itemStack) * cooldownSpeedMultiplier) * speedBonus * 20);
    }

    public static void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, int effectLevel, LivingEntity target) {
        if (hand == Hand.OFF_HAND && item.getEffectLevel(itemStack, ItemEffect.abilityDefensive) > 0) {
            performDefensive(attacker, item, itemStack, target);
        } else {
            performRegular(attacker, item, itemStack, damageMultiplier, effectLevel, target);
        }

        target.getEntityWorld().playSound(attacker, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, SoundCategory.PLAYERS, 0.8f, 0.8f);

        attacker.addExhaustion(0.05f);
        attacker.swing(hand, false);
        attacker.getCooldownTracker().setCooldown(item, getCooldown(item, itemStack));

        if (ComboPoints.canSpend(item, itemStack)) {
            ComboPoints.reset(attacker);
        }

        item.tickProgression(attacker, itemStack, 2);
        item.applyDamage(2, itemStack, attacker);
    }

    public static AbilityUseResult performRegular(PlayerEntity attacker, ItemModularHandheld item, ItemStack itemStack, double damageMultiplier,
            int amplifier, LivingEntity target) {
        int comboPoints = ComboPoints.get(attacker);

        int comboLevel = item.getEffectLevel(itemStack, ItemEffect.abilityCombo);
        if (comboLevel > 0) {
            damageMultiplier += comboLevel * comboPoints / 100d;
        }

        AbilityUseResult result = item.hitEntity(itemStack, attacker, target, damageMultiplier, 0.2f, 0.2f);

        if (result != AbilityUseResult.fail) {
            int currentAmplifier = Optional.ofNullable(target.getActivePotionEffect(PriedPotionEffect.instance))
                    .map(EffectInstance::getAmplifier)
                    .orElse(-1);

            double comboEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityCombo);
            if (comboEfficiency > 0 && attacker.getEntityWorld().getRandom().nextFloat() < (comboEfficiency * comboPoints / 100f)) {
                amplifier++;

                if (!target.getEntityWorld().isRemote) {
                    Random rand = target.getEntityWorld().getRandom();
                    ((ServerWorld) target.getEntityWorld()).spawnParticle(ParticleTypes.CRIT,
                            target.getPosX(), target.getPosY() + target.getHeight() / 2, target.getPosZ(), 10,
                            rand.nextGaussian() * 0.3, rand.nextGaussian() * target.getHeight() * 0.8, rand.nextGaussian() * 0.3, 0.1f);
                }
            }

            target.addPotionEffect(new EffectInstance(PriedPotionEffect.instance, (int) (item.getEffectEfficiency(itemStack, ItemEffect.pry) * 20),
                    currentAmplifier + amplifier, false, false));

            if (!target.getEntityWorld().isRemote) {
                ParticleHelper.spawnArmorParticles((ServerWorld) target.getEntityWorld(), target);
            }

            int momentumLevel = item.getEffectLevel(itemStack, ItemEffect.abilityMomentum);
            if (momentumLevel > 0 && currentAmplifier > -1) {
                int duration = momentumLevel * (currentAmplifier + 1);
                target.addPotionEffect(new EffectInstance(StunPotionEffect.instance, duration, 0, false, false));
            }
        }

        return result;
    }

    public static AbilityUseResult performDefensive(PlayerEntity attacker, ItemModularHandheld item, ItemStack itemStack, LivingEntity target) {
        AbilityUseResult result = item.hitEntity(itemStack, attacker, target, 0.5, 0.2f, 0.2f);

        if (result != AbilityUseResult.fail) {
            target.addPotionEffect(new EffectInstance(Effects.WEAKNESS, (int) (item.getEffectEfficiency(itemStack, ItemEffect.abilityDefensive) * 20),
                    item.getEffectLevel(itemStack, ItemEffect.abilityDefensive) - 1, false, true));

            if (!target.getEntityWorld().isRemote) {
                if (target.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
                    ParticleHelper.spawnArmorParticles((ServerWorld) target.getEntityWorld(), target, EquipmentSlotType.MAINHAND);
                } else if (target.hasItemInSlot(EquipmentSlotType.OFFHAND)) {
                    ParticleHelper.spawnArmorParticles((ServerWorld) target.getEntityWorld(), target, EquipmentSlotType.OFFHAND);
                }
            }
        }

        return result;
    }
}
