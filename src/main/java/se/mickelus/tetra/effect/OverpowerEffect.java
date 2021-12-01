package se.mickelus.tetra.effect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.effect.potion.ExhaustedPotionEffect;
import se.mickelus.tetra.effect.revenge.RevengeTracker;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
@ParametersAreNonnullByDefault
public class OverpowerEffect extends ChargedAbilityEffect {
    private static Cache<Integer, DelayData> delayCache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    public static final OverpowerEffect instance = new OverpowerEffect();

    OverpowerEffect() {
        super(10, 1f, 10, 1, ItemEffect.overpower, TargetRequirement.none, UseAnim.SPEAR, "raised");
    }


    public void perform(Player attacker, InteractionHand hand, ItemModularHandheld item, ItemStack itemStack,
            @Nullable LivingEntity target, @Nullable BlockPos targetPos, @Nullable Vec3 hitVec, int chargedTicks) {
        super.perform(attacker, hand, item, itemStack, target, targetPos, hitVec, chargedTicks);

        boolean isDefensive = isDefensive(item, itemStack, hand);
        int overchargeBonus = canOvercharge(item, itemStack) ? getOverchargeBonus(item, itemStack, chargedTicks) : 0;
        int revengeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityRevenge);
        boolean overextended = item.getEffectLevel(itemStack, ItemEffect.abilityOverextend) > 0;

        double exhaustDuration = item.getEffectEfficiency(itemStack, ItemEffect.overpower);

        if (!attacker.level.isClientSide && !isDefensive) {
            int currentAmp = Optional.ofNullable(attacker.getEffect(ExhaustedPotionEffect.instance))
                    .map(MobEffectInstance::getAmplifier)
                    .orElse(-1);

            int newAmp = 1;

            if (overchargeBonus > 0) {
                newAmp += (int) (overchargeBonus * item.getEffectEfficiency(itemStack, ItemEffect.abilityOvercharge));
            }

            double comboEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityCombo);
            if (comboEfficiency > 0 && attacker.getCommandSenderWorld().getRandom().nextFloat() < (comboEfficiency * ComboPoints.get(attacker) / 100f)) {
                newAmp--;

                Random rand = attacker.getCommandSenderWorld().getRandom();
                ((ServerLevel) attacker.getCommandSenderWorld()).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        attacker.getX(), attacker.getY() + attacker.getBbHeight() / 2, attacker.getZ(), 10,
                        rand.nextGaussian() * 0.3, rand.nextGaussian() * attacker.getBbHeight() * 0.8, rand.nextGaussian() * 0.3, 0.1f);
            }

            if (revengeLevel > 0 && RevengeTracker.canRevenge(attacker, target)) {
                newAmp--;
            }

            if (overextended && !attacker.getFoodData().needsFood()) {
                newAmp = -1;
            }

            if (newAmp > 0) {
                int echoLevel = item.getEffectLevel(itemStack, ItemEffect.abilityEcho);
                if (echoLevel > 0) {
                    delayExhaustion(attacker, item, itemStack, (int) (exhaustDuration * 20), newAmp);
                } else {
                    attacker.addEffect(new MobEffectInstance(ExhaustedPotionEffect.instance, (int) (exhaustDuration * 20),
                            newAmp + currentAmp, false, true));
                }
            }
        }

        attacker.causeFoodExhaustion(overextended ? 6 : 1);
        attacker.swing(hand, false);

        int cooldown = getCooldown(item, itemStack);

        if (isDefensive) {
            cooldown = (int) (cooldown * (1 + item.getEffectEfficiency(itemStack, ItemEffect.abilityDefensive) / 100f));
        }

        if (ComboPoints.canSpend(item, itemStack)) {
            ComboPoints.reset(attacker);
        }

        if (revengeLevel > 0) {
            RevengeTracker.removeEnemy(attacker, target);
        }

        attacker.getCooldowns().addCooldown(item, cooldown);
    }

    @Override
    public void perform(Player attacker, InteractionHand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vec3 hitVec, int chargedTicks) {
        boolean isDefensive = isDefensive(item, itemStack, hand);
        int overchargeBonus = canOvercharge(item, itemStack) ? getOverchargeBonus(item, itemStack, chargedTicks) : 0;
        int revengeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityRevenge);

        double damageMultiplier = item.getEffectLevel(itemStack, isDefensive ? ItemEffect.abilityDefensive : ItemEffect.overpower) / 100f;
        double efficiency = item.getEffectEfficiency(itemStack, ItemEffect.overpower);

        if (overchargeBonus > 0) {
            damageMultiplier += overchargeBonus * item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge) / 100d;
        }

        int comboLevel = item.getEffectLevel(itemStack, ItemEffect.abilityCombo);
        if (comboLevel > 0) {
            damageMultiplier += comboLevel * ComboPoints.get(attacker) / 100d;
        }

        if (revengeLevel > 0 && RevengeTracker.canRevenge(attacker, target)) {
            damageMultiplier += revengeLevel / 100d;
        }

        AbilityUseResult result = item.hitEntity(itemStack, attacker, target, damageMultiplier, 0.1f, 0.1f);
        if (result != AbilityUseResult.fail) {
            int currentAmplifier = Optional.ofNullable(target.getEffect(ExhaustedPotionEffect.instance))
                    .map(MobEffectInstance::getAmplifier)
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

                velocity += momentumEfficiency * Optional.ofNullable(attacker.getEffect(ExhaustedPotionEffect.instance))
                        .map(MobEffectInstance::getAmplifier)
                        .map(amp -> amp + 1)
                        .orElse(0);

                velocity *= 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);

                if (velocity > 0) {
                    target.push(0, velocity, 0);
                }
            }


            int exhilarationLevel = item.getEffectLevel(itemStack, ItemEffect.abilityExhilaration);
            if (exhilarationLevel > 0 && !target.isAlive()) {
                ServerScheduler.schedule(0, () -> attacker.removeEffect(ExhaustedPotionEffect.instance));

            }

            target.addEffect(new MobEffectInstance(ExhaustedPotionEffect.instance, (int) (efficiency * 20), amplifier, false, true));

            target.getCommandSenderWorld().playSound(attacker, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1, 0.8f);
        } else {
            target.getCommandSenderWorld().playSound(attacker, target.blockPosition(), SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 1, 0.8f);
        }


        item.tickProgression(attacker, itemStack, result == AbilityUseResult.fail ? 1 : 2);
        item.applyDamage(2, itemStack, attacker);
    }

    private void delayExhaustion(Player attacker, ItemModularHandheld item, ItemStack itemStack, int duration, int amplifier) {
        int delay = getChargeTime(item, itemStack) + getCooldown(item, itemStack) + item.getEffectLevel(itemStack, ItemEffect.abilityEcho);

        try {
            DelayData data = delayCache.get(attacker.getId(), DelayData::new);
            data.timestamp = attacker.level.getGameTime() + delay;
            data.amplifier += amplifier;
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        ServerScheduler.schedule(delay + 1, () -> {
            DelayData data = delayCache.getIfPresent(attacker.getId());
            if (attacker.isAlive() && attacker.level != null && data != null && attacker.level.getGameTime() > data.timestamp) {
                int currentAmp = Optional.ofNullable(attacker.getEffect(ExhaustedPotionEffect.instance))
                        .map(MobEffectInstance::getAmplifier)
                        .orElse(-1);
                attacker.addEffect(new MobEffectInstance(ExhaustedPotionEffect.instance, duration, currentAmp + data.amplifier, false, true));

                delayCache.invalidate(attacker.getId());
            }
        });
    }

    static class DelayData {
        int amplifier;
        long timestamp;
    }
}
