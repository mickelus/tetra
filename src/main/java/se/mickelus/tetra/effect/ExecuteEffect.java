package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import se.mickelus.tetra.effect.potion.SeveredPotionEffect;
import se.mickelus.tetra.effect.potion.StunPotionEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;

import java.util.Optional;
import java.util.Random;

public class ExecuteEffect extends ChargedAbilityEffect {

    public static final ExecuteEffect instance = new ExecuteEffect();

    ExecuteEffect() {
        super(20, 0.5f, 40, 8, ItemEffect.execute, TargetRequirement.entity, UseAction.SPEAR, "raised");
    }

    @Override
    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vector3d hitVec, int chargedTicks) {
        if (!target.world.isRemote) {
            AbilityUseResult result;
            if (isDefensive(item, itemStack, hand)) {
                result = defensiveExecute(attacker, item, itemStack, target);
            } else {
                result = regularExecute(attacker, item, itemStack, target, chargedTicks);
            }

            playEffects(result != AbilityUseResult.fail, target, hitVec);

            item.tickProgression(attacker, itemStack, result == AbilityUseResult.fail ? 1 : 2);
        }

        attacker.addExhaustion(1f);
        attacker.swing(hand, false);
        attacker.getCooldownTracker().setCooldown(item, getCooldown(item, itemStack));

        if (ComboPoints.canSpend(item, itemStack)) {
            ComboPoints.reset(attacker);
        }

        item.applyDamage(2, itemStack, attacker);
    }

    private AbilityUseResult regularExecute(PlayerEntity attacker, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, int chargedTicks) {
        long harmfulCount = target.getActivePotionEffects().stream()
                .filter(effect -> effect.getPotion().getEffectType() == EffectType.HARMFUL)
                .mapToInt(EffectInstance::getAmplifier)
                .map(amp -> amp + 1)
                .sum();

        if (target.isBurning()) {
            harmfulCount++;
        }

        float missingHealth = MathHelper.clamp(1 - target.getHealth() / target.getMaxHealth(), 0, 1);
        double efficiency = item.getEffectEfficiency(itemStack, ItemEffect.execute);

        double damageMultiplier = missingHealth + harmfulCount * efficiency / 100;

        double comboLevel = item.getEffectLevel(itemStack, ItemEffect.abilityCombo);
        if (comboLevel > 0) {
            damageMultiplier *= 1 + comboLevel * ComboPoints.get(attacker) / 100;
        }

        damageMultiplier += 1;

        if (canOvercharge(item, itemStack)) {
            damageMultiplier *= 1 + getOverchargeBonus(item, itemStack, chargedTicks) * item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge) / 100d;
        }

        double revengeMultiplier = getRevengeMultiplier(attacker, item, itemStack);
        if (revengeMultiplier > 0) {
            damageMultiplier *= revengeMultiplier;
        }

        AbilityUseResult result = item.hitEntity(itemStack, attacker, target, damageMultiplier, 0.2f, 0.2f);

        if (result != AbilityUseResult.fail) {
            int momentumLevel = item.getEffectLevel(itemStack, ItemEffect.abilityMomentum);
            if (momentumLevel > 0) {
                int duration = (int) (momentumLevel * damageMultiplier * 20);
                target.addPotionEffect(new EffectInstance(StunPotionEffect.instance, duration, 0, false, false));
            }
        }

        return result;
    }

    private AbilityUseResult defensiveExecute(PlayerEntity attacker, ItemModularHandheld item, ItemStack itemStack, LivingEntity target) {
        boolean targetFullHealth = target.getMaxHealth() == target.getHealth();
        double damageMultiplier = item.getEffectLevel(itemStack, ItemEffect.abilityDefensive) / 100f;

        if (targetFullHealth) {
            damageMultiplier += item.getEffectEfficiency(itemStack, ItemEffect.abilityDefensive) / 100f;
        }

        AbilityUseResult result = item.hitEntity(itemStack, attacker, target, damageMultiplier, 0.2f, 0.2f);

        if (result != AbilityUseResult.fail) {
            int amp = Optional.ofNullable(target.getActivePotionEffect(SeveredPotionEffect.instance))
                    .map(EffectInstance::getAmplifier)
                    .orElse(-1);
            amp += targetFullHealth ? 2 : 1;
            amp = Math.min(amp, 2);

            target.addPotionEffect(new EffectInstance(SeveredPotionEffect.instance, 1200, amp, false, false));
        }

        return result;
    }

    private double getRevengeMultiplier(PlayerEntity player, ItemModularHandheld item, ItemStack itemStack) {
        int revengeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityRevenge);
        if (revengeLevel > 0 && (player.getActivePotionEffects().stream().anyMatch(effect -> effect.getPotion().getEffectType() == EffectType.HARMFUL)
                || player.isBurning())) {
            return 1 + revengeLevel / 100d;
        }

        return 0;
    }

    private void playEffects(boolean isSuccess, LivingEntity target, Vector3d hitVec) {
        if (isSuccess) {
            target.getEntityWorld().playSound(null, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 1, 0.8f);

            Random rand = target.getRNG();
            CastOptional.cast(target.world, ServerWorld.class).ifPresent(world ->
                    world.spawnParticle(new RedstoneParticleData(0.6f, 0, 0, 0.8f),
                            hitVec.x, hitVec.y, hitVec.z, 10,
                            rand.nextGaussian() * 0.3, rand.nextGaussian() * 0.3, rand.nextGaussian() * 0.3, 0.1f));
        } else {
            target.getEntityWorld().playSound(null, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, SoundCategory.PLAYERS, 1, 0.8f);
        }
    }
}
