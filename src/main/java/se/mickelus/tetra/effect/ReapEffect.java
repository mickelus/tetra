package se.mickelus.tetra.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.effect.potion.*;
import se.mickelus.tetra.effect.revenge.RevengeTracker;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
@ParametersAreNonnullByDefault
public class ReapEffect extends ChargedAbilityEffect {

    public static final ReapEffect instance = new ReapEffect();

    ReapEffect() {
        super(20, 0.7f, 40, 8, ItemEffect.reap, TargetRequirement.none, UseAnim.SPEAR, "raised");
    }

    @Override
    public void perform(Player attacker, InteractionHand hand, ItemModularHandheld item, ItemStack itemStack, @Nullable LivingEntity target, @Nullable BlockPos targetPos, @Nullable Vec3 hitVec, int chargedTicks) {
        if (!attacker.level.isClientSide) {
            int overchargeBonus = canOvercharge(item, itemStack) ? getOverchargeBonus(item, itemStack, chargedTicks) : 0;
            double momentumEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityMomentum);
            int revengeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityRevenge);
            int overextendLevel = item.getEffectLevel(itemStack, ItemEffect.abilityOverextend);
            boolean overextend = overextendLevel > 0 && !attacker.getFoodData().needsFood();
            ServerPlayer serverPlayer = (ServerPlayer) attacker;

            int cooldown = getCooldown(item, itemStack);
            double damageMultiplier = EffectHelper.getEffectLevel(itemStack, ItemEffect.reap) / 100d;
            double range = EffectHelper.getEffectEfficiency(itemStack, ItemEffect.reap);

            if (overchargeBonus > 0) {
                damageMultiplier += overchargeBonus * item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge) / 100d;
                range += overchargeBonus * 0.5;
            }

            int comboLevel = item.getEffectLevel(itemStack, ItemEffect.abilityCombo);
            int comboPoints = ComboPoints.get(attacker);
            if (comboLevel > 0) {
                damageMultiplier += comboLevel * comboPoints / 100d;
            }

            AtomicInteger kills = new AtomicInteger();
            AtomicInteger revengeKills = new AtomicInteger();
            AtomicInteger hits = new AtomicInteger();
            Vec3 targetVec;
            if (target != null) {
                targetVec = hitVec;
            } else {
                targetVec = Vec3.directionFromRotation(attacker.getXRot(), attacker.getYRot())
                        .normalize()
                        .scale(range)
                        .add(attacker.getEyePosition(0));
            }

            AABB aoe = new AABB(targetVec, targetVec).inflate(range, 1d, range);

            hitEntities(serverPlayer, item, itemStack, aoe, damageMultiplier, revengeLevel, overextend, overextendLevel, momentumEfficiency,
                    kills, revengeKills, hits);

            applyBuff(attacker, kills.get(), hits.get(), hand, item, itemStack, chargedTicks, comboPoints, revengeKills.get());

            attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, attacker.getSoundSource(), 1.0F, 1.0F);

            item.tickProgression(attacker, itemStack, 1 + kills.get());

            attacker.sweepAttack();

            attacker.causeFoodExhaustion(overextendLevel > 0 ? 6 : 1);

            double exhilarationEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityExhilaration);
            if (exhilarationEfficiency > 0 && kills.get() > 0) {
                cooldown = (int) (cooldown * (1 - exhilarationEfficiency / 100d));
            }

            attacker.getCooldowns().addCooldown(item, cooldown);

            int echoLevel = item.getEffectLevel(itemStack, ItemEffect.abilityEcho);
            if (echoLevel > 0) {
                echoReap(serverPlayer, hand, item, itemStack, chargedTicks, aoe, damageMultiplier, revengeLevel, overextend, overextendLevel,
                        momentumEfficiency, comboPoints);
            }
        }
        attacker.swing(hand, false);

        if (ComboPoints.canSpend(item, itemStack)) {
            ComboPoints.reset(attacker);
        }

        item.applyDamage(2, itemStack, attacker);
    }

    private void hitEntities(ServerPlayer player, ItemModularHandheld item, ItemStack itemStack, AABB aoe,
            double damageMultiplier, int revengeLevel, boolean overextend, int overextendLevel, double momentumEfficiency,
            AtomicInteger kills, AtomicInteger revengeKills, AtomicInteger hits) {
        Collection<LivingEntity> momentumTargets = new LinkedList<>();
        player.level.getEntitiesOfClass(LivingEntity.class, aoe).stream()
                .filter(entity -> entity != player)
                .filter(entity -> !player.isAlliedTo(entity))
                .forEach(entity -> {
                    double individualDamageMultiplier = damageMultiplier;

                    boolean canRevenge = revengeLevel > 0 && RevengeTracker.canRevenge(player, entity);
                    if (canRevenge) {
                        individualDamageMultiplier += revengeLevel / 100d;
                    }

                    if (overextend && entity.getHealth() / entity.getMaxHealth() >= overextendLevel / 100f) {
                        individualDamageMultiplier *= 2;
                    }

                    AbilityUseResult result = item.hitEntity(itemStack, player, entity, individualDamageMultiplier, 0.5f, 0.2f);
                    if (result != AbilityUseResult.fail) {
                        if (!entity.isAlive()) {
                            kills.incrementAndGet();

                            if (canRevenge) {
                                revengeKills.incrementAndGet();
                                RevengeTracker.removeEnemySynced(player, entity);
                            }
                        } else if (momentumEfficiency > 0) {
                            momentumTargets.add(entity);
                        }

                        hits.incrementAndGet();
                    }

                    if (result == AbilityUseResult.crit) {
                        player.getCommandSenderWorld().playSound(player, entity.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1, 1.3f);
                    }
                });

        if (momentumEfficiency > 0 && kills.get() > 0) {
            int stunDuration = (int) (momentumEfficiency * kills.get() * 20);
            momentumTargets.forEach(entity -> entity.addEffect(new MobEffectInstance(StunPotionEffect.instance, stunDuration, 0, false, false)));
        }
    }

    private void applyBuff(Player attacker, int kills, int hits, InteractionHand hand, ItemModularHandheld item, ItemStack itemStack, int chargedTicks,
            int comboPoints, int revengeKills) {
        int defensiveLevel = item.getEffectLevel(itemStack, ItemEffect.abilityDefensive);
        if (defensiveLevel > 0) {
            if (hand == InteractionHand.OFF_HAND) {
                if (hits > 0) {
                    int duration = defensiveLevel * (1 + kills * 2);
                    attacker.addEffect(new MobEffectInstance(SteeledPotionEffect.instance, duration, hits - 1, false, true));
                }
            } else if (kills > 0) {
                int duration = (int) (item.getEffectEfficiency(itemStack, ItemEffect.abilityDefensive) * 20);
                attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, kills - 1, false, true));
            }
        }

        if (kills > 0) {
            int overchargeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge);
            if (overchargeLevel > 0) {
                double duration = 30 * 20;

                duration *= 1 + getOverchargeBonus(item, itemStack, chargedTicks) * item.getEffectEfficiency(itemStack, ItemEffect.abilityOvercharge);

                attacker.addEffect(new MobEffectInstance(SmallStrengthPotionEffect.instance, (int) duration, kills - 1, false, true));
            }

            int speedLevel = item.getEffectLevel(itemStack, ItemEffect.abilitySpeed);
            if (speedLevel > 0) {
                attacker.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, (int) (item.getEffectEfficiency(itemStack, ItemEffect.abilitySpeed) * 20),
                        kills - 1, false, true));
            }

            int momentumLevel = item.getEffectLevel(itemStack, ItemEffect.abilityMomentum);
            if (momentumLevel > 0) {
                attacker.addEffect(new MobEffectInstance(UnwaveringPotionEffect.instance, momentumLevel * kills * 20,
                        0, false, true));
            }

            double comboEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityCombo);
            if (comboEfficiency > 0) {
                double duration = 15 * 20;

                duration += comboEfficiency * comboPoints * 20;

                attacker.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, (int) duration, kills - 1, false, true));
            }


            if (revengeKills > 0) {
                double duration = 20 * 20;
                duration += item.getEffectEfficiency(itemStack, ItemEffect.abilityRevenge) * revengeKills * 20;

                attacker.addEffect(new MobEffectInstance(SmallStrengthPotionEffect.instance, (int) duration, kills - 1, false, true));
            }

            int exhilarationLevel = item.getEffectLevel(itemStack, ItemEffect.abilityExhilaration);
            if (exhilarationLevel > 0) {
                int currentAmplifier = Optional.ofNullable(attacker.getEffect(SmallAbsorbPotionEffect.instance))
                        .map(MobEffectInstance::getAmplifier)
                        .orElse(-1);
                int amp = Math.max(currentAmplifier, kills - 1);
                attacker.addEffect(new MobEffectInstance(SmallAbsorbPotionEffect.instance, 30 * 20, amp, false, true));
            }

            int echoLevel = item.getEffectLevel(itemStack, ItemEffect.abilityEcho);
            if (echoLevel > 0) {
                int amp = Optional.ofNullable(attacker.getEffect(SmallStrengthPotionEffect.instance))
                        .map(MobEffectInstance::getAmplifier)
                        .orElse(-1);
                amp = Math.min(echoLevel, amp + kills);
                attacker.addEffect(new MobEffectInstance(SmallStrengthPotionEffect.instance, 30 * 20, amp, false, true));
            }
        }

        int overextendLevel = item.getEffectLevel(itemStack, ItemEffect.abilityOverextend);
        if (overextendLevel > 0) {
            if (kills > 0) {
                attacker.addEffect(new MobEffectInstance(SmallHealthPotionEffect.instance, 45 * 20, kills - 1, false, true));
            } else if (!attacker.getFoodData().needsFood()) {
                attacker.addEffect(new MobEffectInstance(ExhaustedPotionEffect.instance, 20 * 20, 4, false, true));
                attacker.causeFoodExhaustion(12);
            }
        }
    }

    private void echoReap(ServerPlayer player, InteractionHand hand, ItemModularHandheld item, ItemStack itemStack, int chargedTicks, AABB aoe,
            double damageMultiplier, int revengeLevel, boolean overextend, int overextendLevel, double momentumEfficiency, int comboPoints) {
        EchoHelper.echo(player, 60, () -> {
            AtomicInteger kills = new AtomicInteger();
            AtomicInteger revengeKills = new AtomicInteger();
            AtomicInteger hits = new AtomicInteger();
            hitEntities(player, item, itemStack, aoe, damageMultiplier, revengeLevel, overextend, overextendLevel, momentumEfficiency,
                    kills, revengeKills, hits);

            applyBuff(player, kills.get(), hits.get(), hand, item, itemStack, chargedTicks, comboPoints, revengeKills.get());
            player.sweepAttack();
        });
    }
}
