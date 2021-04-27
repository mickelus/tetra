package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import se.mickelus.tetra.effect.potion.*;
import se.mickelus.tetra.effect.revenge.RevengeTracker;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ReapEffect extends ChargedAbilityEffect {

    public static final ReapEffect instance = new ReapEffect();

    ReapEffect() {
        super(20, 0.7f, 40, 8, ItemEffect.reap, TargetRequirement.none, UseAction.SPEAR, "raised");
    }

    @Override
    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, @Nullable LivingEntity target, @Nullable BlockPos targetPos, @Nullable Vector3d hitVec, int chargedTicks) {
        if (!attacker.world.isRemote) {
            int overchargeBonus = canOvercharge(item, itemStack) ? getOverchargeBonus(item, itemStack, chargedTicks) : 0;
            double momentumEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityMomentum);
            int revengeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityRevenge);
            int overextendLevel = item.getEffectLevel(itemStack, ItemEffect.abilityOverextend);
            boolean overextend = overextendLevel > 0 && !attacker.getFoodStats().needFood();
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) attacker;

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
            Vector3d targetVec;
            if (target != null) {
                targetVec = hitVec;
            } else {
                targetVec = Vector3d.fromPitchYaw(attacker.rotationPitch, attacker.rotationYaw)
                        .normalize()
                        .scale(range)
                        .add(attacker.getEyePosition(0));
            }

            AxisAlignedBB aoe = new AxisAlignedBB(targetVec, targetVec).grow(range, 1d, range);

            hitEntities(serverPlayer, item, itemStack, aoe, damageMultiplier, revengeLevel, overextend, overextendLevel, momentumEfficiency,
                    kills, revengeKills, hits);

            applyBuff(attacker, kills.get(), hits.get(), hand, item, itemStack, chargedTicks, comboPoints, revengeKills.get());

            attacker.world.playSound(null, attacker.getPosX(), attacker.getPosY(), attacker.getPosZ(),
                    SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, attacker.getSoundCategory(), 1.0F, 1.0F);

            item.tickProgression(attacker, itemStack, 1 + kills.get());

            attacker.spawnSweepParticles();

            attacker.addExhaustion(overextendLevel > 0 ? 6 : 1);

            double exhilarationEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityExhilaration);
            if (exhilarationEfficiency > 0 && kills.get() > 0) {
                cooldown = (int) (cooldown * (1 - exhilarationEfficiency / 100d));
            }

            attacker.getCooldownTracker().setCooldown(item, cooldown);

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

    private void hitEntities(ServerPlayerEntity player, ItemModularHandheld item, ItemStack itemStack, AxisAlignedBB aoe,
            double damageMultiplier, int revengeLevel, boolean overextend, int overextendLevel, double momentumEfficiency,
            AtomicInteger kills, AtomicInteger revengeKills, AtomicInteger hits) {
        Collection<LivingEntity> momentumTargets = new LinkedList<>();
        player.world.getEntitiesWithinAABB(LivingEntity.class, aoe).stream()
                .filter(entity -> entity != player)
                .filter(entity -> !player.isOnSameTeam(entity))
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
                        player.getEntityWorld().playSound(player, entity.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1, 1.3f);
                    }
                });

        if (momentumEfficiency > 0 && kills.get() > 0) {
            int stunDuration = (int) (momentumEfficiency * kills.get() * 20);
            momentumTargets.forEach(entity -> entity.addPotionEffect(new EffectInstance(StunPotionEffect.instance, stunDuration, 0, false, false)));
        }
    }

    private void applyBuff(PlayerEntity attacker, int kills, int hits, Hand hand, ItemModularHandheld item, ItemStack itemStack, int chargedTicks,
            int comboPoints, int revengeKills) {
        int defensiveLevel = item.getEffectLevel(itemStack, ItemEffect.abilityDefensive);
        if (defensiveLevel > 0) {
            if (hand == Hand.OFF_HAND) {
                if (hits > 0) {
                    int duration = defensiveLevel * (1 + kills * 2);
                    attacker.addPotionEffect(new EffectInstance(SteeledPotionEffect.instance, duration, hits - 1, false, true));
                }
            } else if (kills > 0) {
                int duration = (int) (item.getEffectEfficiency(itemStack, ItemEffect.abilityDefensive) * 20);
                attacker.addPotionEffect(new EffectInstance(Effects.SPEED, duration, kills - 1, false, true));
            }
        }

        if (kills > 0) {
            int overchargeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge);
            if (overchargeLevel > 0) {
                double duration = 30 * 20;

                duration *= 1 + getOverchargeBonus(item, itemStack, chargedTicks) * item.getEffectEfficiency(itemStack, ItemEffect.abilityOvercharge);

                attacker.addPotionEffect(new EffectInstance(SmallStrengthPotionEffect.instance, (int) duration, kills - 1, false, true));
            }

            int speedLevel = item.getEffectLevel(itemStack, ItemEffect.abilitySpeed);
            if (speedLevel > 0) {
                attacker.addPotionEffect(new EffectInstance(Effects.HASTE, (int) (item.getEffectEfficiency(itemStack, ItemEffect.abilitySpeed) * 20),
                        kills - 1, false, true));
            }

            int momentumLevel = item.getEffectLevel(itemStack, ItemEffect.abilityMomentum);
            if (momentumLevel > 0) {
                attacker.addPotionEffect(new EffectInstance(UnwaveringPotionEffect.instance, momentumLevel * kills * 20,
                        0, false, true));
            }

            double comboEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityCombo);
            if (comboEfficiency > 0) {
                double duration = 15 * 20;

                duration += comboEfficiency * comboPoints * 20;

                attacker.addPotionEffect(new EffectInstance(Effects.HASTE, (int) duration, kills - 1, false, true));
            }


            if (revengeKills > 0) {
                double duration = 20 * 20;
                duration += item.getEffectEfficiency(itemStack, ItemEffect.abilityRevenge) * revengeKills * 20;

                attacker.addPotionEffect(new EffectInstance(SmallStrengthPotionEffect.instance, (int) duration, kills - 1, false, true));
            }

            int exhilarationLevel = item.getEffectLevel(itemStack, ItemEffect.abilityExhilaration);
            if (exhilarationLevel > 0) {
                int currentAmplifier = Optional.ofNullable(attacker.getActivePotionEffect(SmallAbsorbPotionEffect.instance))
                        .map(EffectInstance::getAmplifier)
                        .orElse(-1);
                int amp = Math.max(currentAmplifier, kills - 1);
                attacker.addPotionEffect(new EffectInstance(SmallAbsorbPotionEffect.instance, 30 * 20, amp, false, true));
            }

            int echoLevel = item.getEffectLevel(itemStack, ItemEffect.abilityEcho);
            if (echoLevel > 0) {
                int amp = Optional.ofNullable(attacker.getActivePotionEffect(SmallStrengthPotionEffect.instance))
                        .map(EffectInstance::getAmplifier)
                        .orElse(-1);
                amp = Math.min(echoLevel, amp + kills);
                attacker.addPotionEffect(new EffectInstance(SmallStrengthPotionEffect.instance, 30 * 20, amp, false, true));
            }
        }

        int overextendLevel = item.getEffectLevel(itemStack, ItemEffect.abilityOverextend);
        if (overextendLevel > 0) {
            if (kills > 0) {
                attacker.addPotionEffect(new EffectInstance(SmallHealthPotionEffect.instance, 45 * 20, kills - 1, false, true));
            } else if (!attacker.getFoodStats().needFood()) {
                attacker.addPotionEffect(new EffectInstance(ExhaustedPotionEffect.instance, 20 * 20, 4, false, true));
                attacker.addExhaustion(12);
            }
        }
    }

    private void echoReap(ServerPlayerEntity player, Hand hand, ItemModularHandheld item, ItemStack itemStack, int chargedTicks, AxisAlignedBB aoe,
            double damageMultiplier, int revengeLevel, boolean overextend, int overextendLevel, double momentumEfficiency, int comboPoints) {
        EchoHelper.echo(player, 60, () -> {
            AtomicInteger kills = new AtomicInteger();
            AtomicInteger revengeKills = new AtomicInteger();
            AtomicInteger hits = new AtomicInteger();
            hitEntities(player, item, itemStack, aoe, damageMultiplier, revengeLevel, overextend, overextendLevel, momentumEfficiency,
                    kills, revengeKills, hits);

            applyBuff(player, kills.get(), hits.get(), hand, item, itemStack, chargedTicks, comboPoints, revengeKills.get());
            player.spawnSweepParticles();
        });
    }
}
