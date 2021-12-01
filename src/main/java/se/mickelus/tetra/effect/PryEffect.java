package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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
import se.mickelus.tetra.effect.revenge.RevengeTracker;
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
        if (!attacker.level.isClientSide) {
            int comboPoints = ComboPoints.get(attacker);
            boolean isSatiated = !attacker.getFoodData().needsFood();

            if (hand == Hand.OFF_HAND && item.getEffectLevel(itemStack, ItemEffect.abilityDefensive) > 0) {
                performDefensive(attacker, item, itemStack, target);
            } else {
                performRegular(attacker, item, itemStack, damageMultiplier, effectLevel, target, isSatiated, comboPoints);
            }

            target.getCommandSenderWorld().playSound(attacker, target.blockPosition(), SoundEvents.PLAYER_ATTACK_WEAK, SoundCategory.PLAYERS, 0.8f, 0.8f);

            boolean overextended = item.getEffectLevel(itemStack, ItemEffect.abilityOverextend) > 0;
            attacker.causeFoodExhaustion(overextended ? 6f : 0.5f);
            attacker.getCooldowns().addCooldown(item, getCooldown(item, itemStack));

            int echoLevel = item.getEffectLevel(itemStack, ItemEffect.abilityEcho);
            if (echoLevel > 0) {
                performEcho(attacker, item, itemStack, damageMultiplier, effectLevel, target, isSatiated, comboPoints);
            }
        }

        if (ComboPoints.canSpend(item, itemStack)) {
            ComboPoints.reset(attacker);
        }

        int revengeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityRevenge);
        if (revengeLevel > 0) {
            RevengeTracker.removeEnemy(attacker, target);
        }

        attacker.swing(hand, false);
        item.tickProgression(attacker, itemStack, 2);
        item.applyDamage(2, itemStack, attacker);
    }

    public static AbilityUseResult performRegular(PlayerEntity attacker, ItemModularHandheld item, ItemStack itemStack, double damageMultiplier,
            int amplifier, LivingEntity target, boolean isSatiated, int comboPoints) {
        int revengeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityRevenge);

        int comboLevel = item.getEffectLevel(itemStack, ItemEffect.abilityCombo);
        if (comboLevel > 0) {
            damageMultiplier += comboLevel * comboPoints / 100d;
        }

        if (revengeLevel > 0 && RevengeTracker.canRevenge(attacker, target)) {
            damageMultiplier += revengeLevel / 100d;
        }

        int exhilarationLevel = item.getEffectLevel(itemStack, ItemEffect.abilityExhilaration);
        if (exhilarationLevel > 0) {
            int amp = Optional.ofNullable(target.getEffect(PriedPotionEffect.instance))
                    .map(EffectInstance::getAmplifier)
                    .orElse(-1) + 1;
            if (amp > 0) {
                damageMultiplier += exhilarationLevel * amp / 100d;
            }
        }

        AbilityUseResult result = item.hitEntity(itemStack, attacker, target, damageMultiplier, 0.2f, 0.2f);

        if (result != AbilityUseResult.fail) {
            int currentAmplifier = Optional.ofNullable(target.getEffect(PriedPotionEffect.instance))
                    .map(EffectInstance::getAmplifier)
                    .orElse(-1);

            double comboEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityCombo);
            if (comboEfficiency > 0 && attacker.getCommandSenderWorld().getRandom().nextFloat() < (comboEfficiency * comboPoints / 100f)) {
                amplifier++;

                if (!target.getCommandSenderWorld().isClientSide) {
                    Random rand = target.getCommandSenderWorld().getRandom();
                    ((ServerWorld) target.getCommandSenderWorld()).sendParticles(ParticleTypes.CRIT,
                            target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 10,
                            rand.nextGaussian() * 0.3, rand.nextGaussian() * target.getBbHeight() * 0.8, rand.nextGaussian() * 0.3, 0.1f);
                }
            }

            if (revengeLevel > 0 && RevengeTracker.canRevenge(attacker, target)) {
                amplifier++;
            }

            double overextendLevel = item.getEffectLevel(itemStack, ItemEffect.abilityOverextend);
            if (overextendLevel > 0 && isSatiated) {
                amplifier++;
            }

            target.addEffect(new EffectInstance(PriedPotionEffect.instance, (int) (item.getEffectEfficiency(itemStack, ItemEffect.pry) * 20),
                    currentAmplifier + amplifier, false, false));

            if (!target.getCommandSenderWorld().isClientSide) {
                ParticleHelper.spawnArmorParticles((ServerWorld) target.getCommandSenderWorld(), target);
            }

            int momentumLevel = item.getEffectLevel(itemStack, ItemEffect.abilityMomentum);
            if (momentumLevel > 0 && currentAmplifier > -1) {
                int duration = momentumLevel * (currentAmplifier + 1);
                target.addEffect(new EffectInstance(StunPotionEffect.instance, duration, 0, false, false));
            }
        }

        return result;
    }

    public static AbilityUseResult performDefensive(PlayerEntity attacker, ItemModularHandheld item, ItemStack itemStack, LivingEntity target) {
        AbilityUseResult result = item.hitEntity(itemStack, attacker, target, 0.5, 0.2f, 0.2f);

        if (result != AbilityUseResult.fail) {
            target.addEffect(new EffectInstance(Effects.WEAKNESS, (int) (item.getEffectEfficiency(itemStack, ItemEffect.abilityDefensive) * 20),
                    item.getEffectLevel(itemStack, ItemEffect.abilityDefensive) - 1, false, true));

            if (!target.getCommandSenderWorld().isClientSide) {
                if (target.hasItemInSlot(EquipmentSlotType.MAINHAND)) {
                    ParticleHelper.spawnArmorParticles((ServerWorld) target.getCommandSenderWorld(), target, EquipmentSlotType.MAINHAND);
                } else if (target.hasItemInSlot(EquipmentSlotType.OFFHAND)) {
                    ParticleHelper.spawnArmorParticles((ServerWorld) target.getCommandSenderWorld(), target, EquipmentSlotType.OFFHAND);
                }
            }
        }

        return result;
    }

    public static void performEcho(PlayerEntity attacker, ItemModularHandheld item, ItemStack itemStack, double damageMultiplier, int amplifier, LivingEntity target,
            boolean isSatiated, int comboPoints) {
        EchoHelper.echo(attacker, 60, () -> {
            performRegular(attacker, item, itemStack, damageMultiplier, amplifier, target, isSatiated, comboPoints);

            int revengeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityRevenge);
            if (revengeLevel > 0) {
                RevengeTracker.removeEnemy(attacker, target);
            }
        });
    }
}
