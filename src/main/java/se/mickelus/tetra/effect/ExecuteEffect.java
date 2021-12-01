package se.mickelus.tetra.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.effect.potion.ExhaustedPotionEffect;
import se.mickelus.tetra.effect.potion.SeveredPotionEffect;
import se.mickelus.tetra.effect.potion.SmallStrengthPotionEffect;
import se.mickelus.tetra.effect.potion.StunPotionEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;

import java.util.Optional;
import java.util.Random;

public class ExecuteEffect extends ChargedAbilityEffect {

    public static final ExecuteEffect instance = new ExecuteEffect();

    ExecuteEffect() {
        super(20, 0.5f, 40, 8, ItemEffect.execute, TargetRequirement.entity, UseAnim.SPEAR, "raised");
    }

    @Override
    public void perform(Player attacker, InteractionHand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vec3 hitVec, int chargedTicks) {
        if (!target.level.isClientSide) {
            AbilityUseResult result;
            if (isDefensive(item, itemStack, hand)) {
                result = defensiveExecute(attacker, item, itemStack, target);
            } else {
                result = regularExecute(attacker, item, itemStack, target, chargedTicks);
            }

            playEffects(result != AbilityUseResult.fail, target, hitVec);

            item.tickProgression(attacker, itemStack, result == AbilityUseResult.fail ? 1 : 2);
        }

        attacker.causeFoodExhaustion(1f);
        attacker.swing(hand, false);
        attacker.getCooldowns().addCooldown(item, getCooldown(item, itemStack));

        if (ComboPoints.canSpend(item, itemStack)) {
            ComboPoints.reset(attacker);
        }

        item.applyDamage(2, itemStack, attacker);
    }

    private AbilityUseResult regularExecute(Player attacker, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, int chargedTicks) {
        long harmfulCount = target.getActiveEffects().stream()
                .filter(effect -> effect.getEffect().getCategory() == MobEffectCategory.HARMFUL)
                .mapToInt(MobEffectInstance::getAmplifier)
                .map(amp -> amp + 1)
                .sum();

        if (target.isOnFire()) {
            harmfulCount++;
        }

        float missingHealth = Mth.clamp(1 - target.getHealth() / target.getMaxHealth(), 0, 1);
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

        int overextendLevel = item.getEffectLevel(itemStack, ItemEffect.abilityOverextend);
        if (overextendLevel > 0) {
            FoodData foodStats = attacker.getFoodData();
            float exhaustion = Math.min(40, foodStats.getFoodLevel() + foodStats.getSaturationLevel());
            damageMultiplier *= 1 + overextendLevel * exhaustion * 0.25 / 100;
            attacker.causeFoodExhaustion(exhaustion); // 4 exhaustion per food/saturation so this should drain 1/4th
        }

        int echoLevel = item.getEffectLevel(itemStack, ItemEffect.abilityEcho);
        if (echoLevel > 0) {
            echoExecute(attacker, item, itemStack, target);
        }

        AbilityUseResult result = item.hitEntity(itemStack, attacker, target, damageMultiplier, 0.2f, 0.2f);

        if (result != AbilityUseResult.fail) {
            int momentumLevel = item.getEffectLevel(itemStack, ItemEffect.abilityMomentum);
            if (momentumLevel > 0) {
                int duration = (int) (momentumLevel * damageMultiplier * 20);
                target.addEffect(new MobEffectInstance(StunPotionEffect.instance, duration, 0, false, false));
            }

            int exhilarationLevel = item.getEffectLevel(itemStack, ItemEffect.abilityExhilaration);
            if (exhilarationLevel > 0 && !target.isAlive()) {
                float maxHealth = target.getMaxHealth();
                int amplifier = Math.round((1 - missingHealth) / exhilarationLevel * 100) - 1;
                int duration = (int) (Math.min(200, item.getEffectEfficiency(itemStack, ItemEffect.abilityExhilaration) * maxHealth) * 20);

                if (amplifier >= 0 && duration > 0) {
                    attacker.addEffect(new MobEffectInstance(SmallStrengthPotionEffect.instance, duration, amplifier, false, true));
                }
            }
        }

        return result;
    }

    private void echoExecute(Player attacker, ItemModularHandheld item, ItemStack itemStack, LivingEntity target) {
        EchoHelper.echo(attacker, 100, () -> {
            long harmfulCount = target.getActiveEffects().stream()
                    .filter(effect -> effect.getEffect().getCategory() == MobEffectCategory.HARMFUL)
                    .mapToInt(MobEffectInstance::getAmplifier)
                    .map(amp -> amp + 1)
                    .sum();

            if (target.isOnFire()) {
                harmfulCount++;
            }

            float missingHealth = Mth.clamp(1 - target.getHealth() / target.getMaxHealth(), 0, 1);
            double damageMultiplier = missingHealth + harmfulCount;

            if (damageMultiplier > 0) {
                AbilityUseResult result = item.hitEntity(itemStack, attacker, target, damageMultiplier, 0.2f, 0.2f);
                playEffects(result != AbilityUseResult.fail, target, target.position().add(0, target.getBbHeight() / 2, 0));
            }
        });
    }

    private AbilityUseResult defensiveExecute(Player attacker, ItemModularHandheld item, ItemStack itemStack, LivingEntity target) {
        boolean targetFullHealth = target.getMaxHealth() == target.getHealth();
        double damageMultiplier = item.getEffectLevel(itemStack, ItemEffect.abilityDefensive) / 100f;

        if (targetFullHealth) {
            damageMultiplier += item.getEffectEfficiency(itemStack, ItemEffect.abilityDefensive) / 100f;
        }

        AbilityUseResult result = item.hitEntity(itemStack, attacker, target, damageMultiplier, 0.2f, 0.2f);

        if (result != AbilityUseResult.fail) {
            int amp = Optional.ofNullable(target.getEffect(SeveredPotionEffect.instance))
                    .map(MobEffectInstance::getAmplifier)
                    .orElse(-1);
            amp += targetFullHealth ? 2 : 1;
            amp = Math.min(amp, 2);

            target.addEffect(new MobEffectInstance(SeveredPotionEffect.instance, 1200, amp, false, false));
        }

        return result;
    }

    private double getRevengeMultiplier(Player player, ItemModularHandheld item, ItemStack itemStack) {
        int revengeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityRevenge);
        if (revengeLevel > 0 && (player.getActiveEffects().stream().anyMatch(effect -> effect.getEffect().getCategory() == MobEffectCategory.HARMFUL)
                || player.isOnFire())) {
            return 1 + revengeLevel / 100d;
        }

        return 0;
    }

    private void playEffects(boolean isSuccess, LivingEntity target, Vec3 hitVec) {
        if (isSuccess) {
            target.getCommandSenderWorld().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1, 0.8f);

            Random rand = target.getRandom();
            CastOptional.cast(target.level, ServerLevel.class).ifPresent(world ->
                    world.sendParticles(new DustParticleOptions(0.6f, 0, 0, 0.8f),
                            hitVec.x, hitVec.y, hitVec.z, 10,
                            rand.nextGaussian() * 0.3, rand.nextGaussian() * 0.3, rand.nextGaussian() * 0.3, 0.1f));
        } else {
            target.getCommandSenderWorld().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 1, 0.8f);
        }
    }
}
