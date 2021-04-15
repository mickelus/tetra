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

public class PryEffect {
    public static final double flatCooldown = 2;
    public static final double cooldownSpeedMultiplier = 3;

    private static int getCooldown(ItemModularHandheld item, ItemStack itemStack) {
        float speedBonus = (100 - item.getEffectLevel(itemStack, ItemEffect.abilitySpeed)) / 100f;
        return (int) ((flatCooldown + item.getCooldownBase(itemStack) * cooldownSpeedMultiplier) * speedBonus * 20);
    }

    public static void perform( PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, int effectLevel, LivingEntity target) {
        if (hand == Hand.OFF_HAND && item.getEffectLevel(itemStack, ItemEffect.abilityDefensive) > 0) {
            performDefensive(attacker, item, itemStack, target);
        } else {
            performRegular(attacker, item, itemStack, effectLevel, target);
        }

        target.getEntityWorld().playSound(attacker, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, SoundCategory.PLAYERS, 0.8f, 0.8f);

        attacker.addExhaustion(0.05f);
        attacker.swing(hand, false);
        attacker.getCooldownTracker().setCooldown(item, getCooldown(item, itemStack));

        item.tickProgression(attacker, itemStack, 2);
        item.applyDamage(2, itemStack, attacker);
    }

    public static AbilityUseResult performRegular(PlayerEntity attacker, ItemModularHandheld item, ItemStack itemStack, int effectLevel, LivingEntity target) {
        int currentAmplifier = Optional.ofNullable(target.getActivePotionEffect(PriedPotionEffect.instance))
                .map(EffectInstance::getAmplifier)
                .orElse(-1);

        AbilityUseResult result = item.hitEntity(itemStack, attacker, target, 0.5, 0.2f, 0.2f);

        if (result != AbilityUseResult.fail) {
            target.addPotionEffect(new EffectInstance(PriedPotionEffect.instance, (int) (item.getEffectEfficiency(itemStack, ItemEffect.pry) * 20),
                    currentAmplifier + effectLevel, false, false));

            if (!target.getEntityWorld().isRemote) {
                ParticleHelper.spawnArmorParticles((ServerWorld) target.getEntityWorld(), target);
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
