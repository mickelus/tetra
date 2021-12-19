package se.mickelus.tetra.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.effect.potion.SmallStrengthPotionEffect;
import se.mickelus.tetra.effect.potion.StunPotionEffect;
import se.mickelus.tetra.effect.revenge.RevengeTracker;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class SlamEffect extends ChargedAbilityEffect {

    public static final SlamEffect instance = new SlamEffect();

    SlamEffect() {
        super(10, 1f, 40, 6, ItemEffect.slam, TargetRequirement.either, UseAnim.SPEAR, "raised");
    }

    private static void groundSlamEntity(Player attacker, LivingEntity target, ItemModularHandheld item, ItemStack itemStack, Vec3 origin,
            double damageMultiplier, int slowDuration, double momentumEfficiency, int revengeLevel) {
        ServerScheduler.schedule(target.blockPosition().distManhattan(new BlockPos(origin)) - 3, () -> {
            float knockback = momentumEfficiency > 0 ? 0.1f : 0.5f;

            AbilityUseResult result = item.hitEntity(itemStack, attacker, target, damageMultiplier, knockback, knockback);

            if (momentumEfficiency > 0) {
                target.getCommandSenderWorld().playSound(null, target.blockPosition(), SoundEvents.GENERIC_BIG_FALL, SoundSource.PLAYERS, 1, 0.7f);
            } else {
                target.getCommandSenderWorld().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1, 0.9f);
            }

            if (result != AbilityUseResult.fail) {
                if (slowDuration > 0) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowDuration, 1, false, true));
                }

                if (momentumEfficiency > 0) {
                    double velocity = momentumEfficiency;
                    velocity *= 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
                    target.push(0, velocity, 0);
                    target.addEffect(new MobEffectInstance(StunPotionEffect.instance, 40, 0, false, false));
                }

                if (revengeLevel > 0 && RevengeTracker.canRevenge(attacker, target)) {
                    target.addEffect(new MobEffectInstance(StunPotionEffect.instance, revengeLevel, 0, false, false));
                    RevengeTracker.removeEnemySynced((ServerPlayer) attacker, target);
                }
            }

            if (result == AbilityUseResult.crit) {
                Random rand = target.getRandom();
                CastOptional.cast(target.level, ServerLevel.class).ifPresent(world ->
                        world.sendParticles(ParticleTypes.CRIT,
                                target.getX(), target.getY(), target.getZ(), 10,
                                rand.nextGaussian() * 0.3, rand.nextGaussian() * 0.5, rand.nextGaussian() * 0.3, 0.1f));
            }
        });
    }

    @Override
    public int getChargeTime(Player attacker, ItemModularHandheld item, ItemStack itemStack) {
        if (ComboPoints.canSpend(item, itemStack)) {
            return (int) (super.getChargeTime(attacker, item, itemStack)
                    * (1 - item.getEffectLevel(itemStack, ItemEffect.abilityCombo) / 100d * ComboPoints.get(attacker)));
        }
        return super.getChargeTime(attacker, item, itemStack);
    }

    @Override
    public void perform(Player attacker, InteractionHand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vec3 hitVec, int chargedTicks) {
        AbilityUseResult result = directSlam(attacker, hand, item, itemStack, target, hitVec, chargedTicks);

        double overextendLevel = item.getEffectLevel(itemStack, ItemEffect.abilityOverextend);
        attacker.causeFoodExhaustion(overextendLevel > 0 ? 6 : 1);
        attacker.swing(hand, false);
        attacker.getCooldowns().addCooldown(item, Math.round(getCooldown(item, itemStack) * 1.5f));

        int revengeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityRevenge);
        if (revengeLevel > 0) {
            RevengeTracker.removeEnemy(attacker, target);
        }

        item.tickProgression(attacker, itemStack, result == AbilityUseResult.fail ? 1 : 2);
        item.applyDamage(2, itemStack, attacker);

        int echoLevel = item.getEffectLevel(itemStack, ItemEffect.abilityEcho);
        if (echoLevel > 0) {
            echoTarget(attacker, hand, item, itemStack, target, hitVec, chargedTicks);
        }
    }

    public AbilityUseResult directSlam(Player attacker, InteractionHand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vec3 hitVec, int chargedTicks) {
        int stunDuration = 0;
        double damageMultiplier = item.getEffectLevel(itemStack, ItemEffect.slam) * 1.5 / 100;
        float knockbackBase = (float) item.getEffectEfficiency(itemStack, ItemEffect.slam);
        float knockbackMultiplier = 1f;

        boolean isDefensive = isDefensive(item, itemStack, hand);
        int overchargeBonus = canOvercharge(item, itemStack) ? getOverchargeBonus(item, itemStack, chargedTicks) : 0;
        int revengeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityRevenge);

        if (isDefensive) {
            damageMultiplier -= 0.3;
            stunDuration = item.getEffectLevel(itemStack, ItemEffect.abilityDefensive);
        }

        if (overchargeBonus > 0) {
            double bonus = overchargeBonus * item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge) / 100d;
            damageMultiplier += bonus;
            knockbackMultiplier += bonus;
        }

        int momentumLevel = item.getEffectLevel(itemStack, ItemEffect.abilityMomentum);
        if (momentumLevel > 0) {
            stunDuration = momentumLevel;
        }

        double momentumEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityMomentum);
        if (momentumEfficiency > 0) {
            knockbackMultiplier = 0.4f;
        }

        double overextendLevel = item.getEffectLevel(itemStack, ItemEffect.abilityOverextend);
        if (overextendLevel > 0 && !attacker.getFoodData().needsFood()) {
            damageMultiplier += overextendLevel / 100d;
        }

        AbilityUseResult result = item.hitEntity(itemStack, attacker, target, damageMultiplier, knockbackMultiplier * knockbackBase, knockbackMultiplier / 2);

        if (result != AbilityUseResult.fail) {
            if (stunDuration > 0) {
                target.addEffect(new MobEffectInstance(StunPotionEffect.instance, stunDuration, 0, false, false));
            }

            if (momentumEfficiency > 0) {
                double velocity = momentumEfficiency;
                velocity *= 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
                target.push(0, velocity, 0);
            }

            if (revengeLevel > 0 && RevengeTracker.canRevenge(attacker, target)) {
                target.addEffect(new MobEffectInstance(StunPotionEffect.instance, revengeLevel, 0, false, false));
            }

            double exhilarationEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityExhilaration);
            if (exhilarationEfficiency > 0) {
                knockbackExhilaration(attacker, attacker.position(), target, target.level.getGameTime() + 200, exhilarationEfficiency);
            }

            Random rand = target.getRandom();
            CastOptional.cast(target.level, ServerLevel.class).ifPresent(world ->
                    world.sendParticles(ParticleTypes.CRIT,
                            hitVec.x, hitVec.y, hitVec.z, 10,
                            rand.nextGaussian() * 0.3, rand.nextGaussian() * target.getBbHeight() * 0.8, rand.nextGaussian() * 0.3, 0.1f));

            target.getCommandSenderWorld().playSound(attacker, target.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1, 0.7f);
        } else {
            target.getCommandSenderWorld().playSound(attacker, target.blockPosition(), SoundEvents.PLAYER_ATTACK_WEAK, SoundSource.PLAYERS, 1, 0.7f);
        }

        return result;
    }

    private void knockbackExhilaration(Player attacker, Vec3 origin, LivingEntity target, long timeLimit, double multiplier) {
        ServerScheduler.schedule(20, () -> {
            if (target.isOnGround()) {
                double distance = Math.min(20, origin.distanceTo(target.position()));
                int amplifier = (int) (distance * multiplier) - 1;
                if (amplifier >= 0) {
                    attacker.addEffect(new MobEffectInstance(SmallStrengthPotionEffect.instance, 200, amplifier, false, true));
                }
            } else if (target.level.getGameTime() < timeLimit) {
                knockbackExhilaration(attacker, origin, target, timeLimit, multiplier);
            }
        });
    }

    @Override
    public void perform(Player attacker, InteractionHand hand, ItemModularHandheld item, ItemStack itemStack, BlockPos targetPos, Vec3 hitVec, int chargedTicks) {
        if (!attacker.level.isClientSide) {
            int overchargeBonus = canOvercharge(item, itemStack) ? getOverchargeBonus(item, itemStack, chargedTicks) : 0;
            int slowDuration = isDefensive(item, itemStack, hand) ? (int) (item.getEffectEfficiency(itemStack, ItemEffect.abilityDefensive) * 20) : 0;
            double momentumEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityMomentum);
            int revengeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityRevenge);
            double overextendLevel = item.getEffectLevel(itemStack, ItemEffect.abilityOverextend);

            double range = getAoeRange(attacker, item, itemStack, overchargeBonus);

            Vec3 direction = hitVec.subtract(attacker.position()).multiply(1, 0, 1).normalize();
            double yaw = Mth.atan2(direction.x, direction.z);
            AABB boundingBox = new AABB(hitVec, hitVec).inflate(range + 1, 4, range + 1).move(direction.scale(range / 2));
            List<LivingEntity> targets = attacker.level.getEntitiesOfClass(LivingEntity.class, boundingBox).stream()
                    .filter(Entity::isAlive)
                    .filter(Entity::isAttackable)
                    .filter(entity -> !attacker.equals(entity))
                    .filter(entity -> inRange(hitVec, entity, yaw, range))
                    .collect(Collectors.toList());

            double damageMultiplier = getAoeDamageMultiplier(attacker, item, itemStack, slowDuration > 0, overchargeBonus, targets);

            targets.forEach(entity -> groundSlamEntity(attacker, entity, item, itemStack, hitVec, damageMultiplier, slowDuration, momentumEfficiency, revengeLevel));

            spawnGroundParticles(attacker.level, hitVec, direction, yaw, range);

            attacker.causeFoodExhaustion(overextendLevel > 0 ? 6 : 1);

            int echoLevel = item.getEffectLevel(itemStack, ItemEffect.abilityEcho);
            if (echoLevel > 0) {
                echoGround(attacker, item, itemStack, hitVec, direction, yaw, range, damageMultiplier * echoLevel / 100d, slowDuration,
                        momentumEfficiency, revengeLevel);
            }
        }

        attacker.swing(hand, false);
        attacker.getCooldowns().addCooldown(item, getCooldown(item, itemStack));

        item.tickProgression(attacker, itemStack, 2);
        item.applyDamage(2, itemStack, attacker);
    }

    private void echoGround(Player attacker, ItemModularHandheld item, ItemStack itemStack, Vec3 hitVec, Vec3 direction, double yaw,
            double range, double damageMultiplier, int slowDuration, double momentumEfficiency, int revengeLevel) {
        EchoHelper.echo(attacker, 60, () -> {
            AABB boundingBox = new AABB(hitVec, hitVec).inflate(range + 1, 4, range + 1).move(direction.scale(range / 2));
            List<LivingEntity> targets = attacker.level.getEntitiesOfClass(LivingEntity.class, boundingBox).stream()
                    .filter(Entity::isAlive)
                    .filter(Entity::isAttackable)
                    .filter(entity -> inRange(hitVec, entity, yaw, range))
                    .collect(Collectors.toList());

            targets.forEach(entity -> groundSlamEntity(attacker, entity, item, itemStack, hitVec, damageMultiplier, slowDuration, momentumEfficiency, revengeLevel));

            spawnGroundParticles(attacker.level, hitVec, direction, yaw, range);
        });
    }

    private void echoTarget(Player attacker, InteractionHand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vec3 hitVec, int chargedTicks) {
        if (!attacker.level.isClientSide) {
            EchoHelper.echo(attacker, 60, () -> {
                directSlam(attacker, hand, item, itemStack, target, hitVec, chargedTicks);

                int revengeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityRevenge);
                if (revengeLevel > 0) {
                    RevengeTracker.removeEnemySynced((ServerPlayer) attacker, target);
                }
            });
        }
    }

    private double getAoeDamageMultiplier(Player attacker, ItemModularHandheld item, ItemStack itemStack, boolean isDefensive, int overchargeBonus,
            List<LivingEntity> targets) {
        double damageMultiplier = item.getEffectLevel(itemStack, ItemEffect.slam) / 100f;

        if (isDefensive) {
            damageMultiplier -= 0.3;
        }

        if (overchargeBonus > 0) {
            damageMultiplier += overchargeBonus * item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge) / 100d;
        }

        double overextendLevel = item.getEffectLevel(itemStack, ItemEffect.abilityOverextend);
        if (overextendLevel > 0 && !attacker.getFoodData().needsFood()) {
            damageMultiplier += overextendLevel / 100d;
        }

        int exhilarationLevel = item.getEffectLevel(itemStack, ItemEffect.abilityExhilaration);
        if (exhilarationLevel > 0) {
            damageMultiplier += targets.size() * exhilarationLevel / 100d;
        }

        return damageMultiplier;
    }

    private double getAoeRange(Player attacker, ItemModularHandheld item, ItemStack itemStack, int overchargeBonus) {
        double range = 8;

        if (overchargeBonus > 0) {
            range += overchargeBonus * item.getEffectEfficiency(itemStack, ItemEffect.abilityOvercharge);
        }

        double overextendEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityOverextend);
        if (overextendEfficiency > 0 && !attacker.getFoodData().needsFood()) {
            range += overextendEfficiency;
        }

        return range;
    }

    private void spawnGroundParticles(Level world, Vec3 origin, Vec3 direction, double yaw, double range) {
        Random rand = world.random;

        BlockState originState = world.getBlockState(new BlockPos(origin));
        ((ServerLevel) world).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, originState),
                origin.x(), origin.y(), origin.z(),
                8, 0, rand.nextGaussian() * 0.1, 0, 0.1);
        world.playSound(null, new BlockPos(origin), originState.getSoundType().getBreakSound(), SoundSource.PLAYERS, 1.5f, 0.5f);

        int bound = (int) Math.ceil(range / 2);

        BlockPos center = new BlockPos(origin.add(direction.scale(range / 2)));
        origin = origin.add(direction.scale(-1));
        BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos(0, 0, 0);
        for (int x = -bound; x <= bound; x++) {
            for (int z = -bound; z <= bound; z++) {
                targetPos.setWithOffset(center, x, 0, z);
                if (compareAngle(origin, targetPos, yaw)) {
                    for (int y = -2; y < 2; y++) {
                        targetPos.setWithOffset(center, x, y, z);
                        BlockState targetState = world.getBlockState(targetPos);
                        if (targetState.canOcclude() && !world.getBlockState(targetPos.above()).canOcclude()) {
                            targetPos.below();

                            double distance = targetPos.distSqr(origin.x, origin.y, origin.z, true);

                            if (distance < range * range) {
                                double yOffset = targetState.getShape(world, targetPos).bounds().maxY;
                                BlockPos particlePos = targetPos.immutable();

                                ServerScheduler.schedule(particlePos.distManhattan(new BlockPos(origin)) - 3, () -> {
                                    ((ServerLevel) world).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, targetState),
                                            particlePos.getX() + 0.5, particlePos.getY() + yOffset, particlePos.getZ() + 0.5,
                                            3, 0, rand.nextGaussian() * 0.1, 0, 0.1);

                                    if (rand.nextFloat() < 0.3f) {
                                        world.playSound(null, particlePos, targetState.getSoundType().getFallSound(), SoundSource.PLAYERS, 1, 0.5f);
                                    }
                                });
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean inRange(Vec3 origin, Entity entity, double originYaw, double range) {
        if (origin.closerThan(entity.position(), range)) {
            Vec3 direction = entity.position().subtract(origin);
            double entityYaw = Mth.atan2(direction.x, direction.z);
            double yawDiff = Math.abs((originYaw - entityYaw + 3 * Math.PI) % (Math.PI * 2) - Math.PI);

            return yawDiff < Math.PI / 6;
        }
        return false;
    }

    private boolean compareAngle(Vec3 originPos, BlockPos.MutableBlockPos offsetPos, double originYaw) {
        Vec3 direction = Vec3.atBottomCenterOf(offsetPos).subtract(originPos);
        double offsetYaw = Mth.atan2(direction.x(), direction.z());

        double yawDiff = Math.abs((originYaw - offsetYaw + 3 * Math.PI) % (Math.PI * 2) - Math.PI);
        return yawDiff < Math.PI / 6;
    }
}
