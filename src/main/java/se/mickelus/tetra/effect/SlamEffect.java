package se.mickelus.tetra.effect;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.effect.potion.SmallStrengthPotionEffect;
import se.mickelus.tetra.effect.potion.StunPotionEffect;
import se.mickelus.tetra.effect.revenge.RevengeTracker;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SlamEffect extends ChargedAbilityEffect {

    public static final SlamEffect instance = new SlamEffect();

    SlamEffect() {
        super(10, 1f, 40, 6, ItemEffect.slam, TargetRequirement.either, UseAction.SPEAR, "raised");
    }

    @Override
    public int getChargeTime(PlayerEntity attacker, ItemModularHandheld item, ItemStack itemStack) {
        if (ComboPoints.canSpend(item, itemStack)) {
            return (int) (super.getChargeTime(attacker, item, itemStack)
                    * (1 - item.getEffectLevel(itemStack, ItemEffect.abilityCombo) / 100d * ComboPoints.get(attacker)));
        }
        return super.getChargeTime(attacker, item, itemStack);
    }

    @Override
    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vector3d hitVec, int chargedTicks) {
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
        if (overextendLevel > 0 && !attacker.getFoodStats().needFood()) {
            damageMultiplier += overextendLevel / 100d;
        }

        AbilityUseResult result = item.hitEntity(itemStack, attacker, target, damageMultiplier, knockbackMultiplier * knockbackBase, knockbackMultiplier / 2);

        if (result != AbilityUseResult.fail) {
            if (stunDuration > 0) {
                target.addPotionEffect(new EffectInstance(StunPotionEffect.instance, stunDuration, 0, false, false));
            }

            if (momentumEfficiency > 0) {
                double velocity = momentumEfficiency;
                velocity *= 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
                target.addVelocity(0, velocity, 0);
            }

            if (revengeLevel > 0 && RevengeTracker.canRevenge(attacker, target)) {
                target.addPotionEffect(new EffectInstance(StunPotionEffect.instance, revengeLevel, 0, false, false));
            }

            double exhilarationEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityExhilaration);
            if (exhilarationEfficiency > 0) {
                knockbackExhilaration(attacker, attacker.getPositionVec(), target, target.world.getGameTime() + 200, exhilarationEfficiency);
            }

            Random rand = target.getRNG();
            CastOptional.cast(target.world, ServerWorld.class).ifPresent(world ->
                    world.spawnParticle(ParticleTypes.CRIT,
                            hitVec.x, hitVec.y, hitVec.z, 10,
                            rand.nextGaussian() * 0.3, rand.nextGaussian() * target.getHeight() * 0.8, rand.nextGaussian() * 0.3, 0.1f));

            target.getEntityWorld().playSound(attacker, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 1, 0.7f);
        } else {
            target.getEntityWorld().playSound(attacker, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, SoundCategory.PLAYERS, 1, 0.7f);
        }

        attacker.addExhaustion(overextendLevel > 0 ? 6 : 1);
        attacker.swing(hand, false);
        attacker.getCooldownTracker().setCooldown(item, Math.round(getCooldown(item, itemStack) * 1.5f));

        if (revengeLevel > 0) {
            RevengeTracker.removeEnemy(attacker, target);
        }

        item.tickProgression(attacker, itemStack, result == AbilityUseResult.fail ? 1 : 2);
        item.applyDamage(2, itemStack, attacker);
    }

    private void knockbackExhilaration(PlayerEntity attacker, Vector3d origin, LivingEntity target, long timeLimit, double multiplier) {
        ServerScheduler.schedule(20, () -> {
            if (target.isOnGround()) {
                double distance = Math.min(20, origin.distanceTo(target.getPositionVec()));
                int amplifier = (int) (distance * multiplier) - 1;
                if (amplifier >= 0) {
                    attacker.addPotionEffect(new EffectInstance(SmallStrengthPotionEffect.instance, 200, amplifier, false, true));
                }
            } else if (target.world.getGameTime() < timeLimit) {
                knockbackExhilaration(attacker, origin, target, timeLimit, multiplier);
            }
        });
    }

    @Override
    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, BlockPos targetPos, Vector3d hitVec, int chargedTicks) {
        if (!attacker.world.isRemote) {
            int overchargeBonus = canOvercharge(item, itemStack) ? getOverchargeBonus(item, itemStack, chargedTicks) : 0;
            int slowDuration = isDefensive(item, itemStack, hand) ? (int) (item.getEffectEfficiency(itemStack, ItemEffect.abilityDefensive) * 20) : 0;
            double momentumEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityMomentum);
            int revengeLevel = item.getEffectLevel(itemStack, ItemEffect.abilityRevenge);
            double overextendLevel = item.getEffectLevel(itemStack, ItemEffect.abilityOverextend);

            double range = getAoeRange(attacker, item, itemStack, overchargeBonus);

            Vector3d direction = hitVec.subtract(attacker.getPositionVec()).mul(1, 0, 1).normalize();
            double yaw = MathHelper.atan2(direction.x, direction.z);
            AxisAlignedBB boundingBox = new AxisAlignedBB(hitVec, hitVec).grow(range + 1, 4, range + 1).offset(direction.scale(range / 2));
            List<LivingEntity> targets = attacker.world.getEntitiesWithinAABB(LivingEntity.class, boundingBox).stream()
                    .filter(Entity::isAlive)
                    .filter(Entity::canBeAttackedWithItem)
                    .filter(entity -> !attacker.equals(entity))
                    .filter(entity -> inRange(hitVec, entity, yaw, range))
                    .collect(Collectors.toList());

            double damageMultiplier = getAoeDamageMultiplier(attacker, item, itemStack, slowDuration > 0, overchargeBonus, targets);

            targets.forEach(entity -> groundSlamEntity(attacker, entity, item, itemStack, hitVec, damageMultiplier, slowDuration, momentumEfficiency, revengeLevel));

            spawnGroundParticles(attacker.world, hitVec, direction, yaw, range);

            attacker.addExhaustion(overextendLevel > 0 ? 6 : 1);
        }

        attacker.swing(hand, false);
        attacker.getCooldownTracker().setCooldown(item, getCooldown(item, itemStack));

        item.tickProgression(attacker, itemStack, 2);
        item.applyDamage(2, itemStack, attacker);
    }

    private double getAoeDamageMultiplier(PlayerEntity attacker, ItemModularHandheld item, ItemStack itemStack, boolean isDefensive, int overchargeBonus,
            List<LivingEntity> targets) {
        double damageMultiplier = item.getEffectLevel(itemStack, ItemEffect.slam) / 100f;

        if (isDefensive) {
            damageMultiplier -= 0.3;
        }

        if (overchargeBonus > 0) {
            damageMultiplier += overchargeBonus * item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge) / 100d;
        }

        double overextendLevel = item.getEffectLevel(itemStack, ItemEffect.abilityOverextend);
        if (overextendLevel > 0 && !attacker.getFoodStats().needFood()) {
            damageMultiplier += overextendLevel / 100d;
        }

        int exhilarationLevel = item.getEffectLevel(itemStack, ItemEffect.abilityExhilaration);
        if (exhilarationLevel > 0) {
            damageMultiplier += targets.size() * exhilarationLevel / 100d;
        }

        return damageMultiplier;
    }

    private double getAoeRange(PlayerEntity attacker, ItemModularHandheld item, ItemStack itemStack, int overchargeBonus) {
        double range = 8;

        if (overchargeBonus > 0) {
            range += overchargeBonus * item.getEffectEfficiency(itemStack, ItemEffect.abilityOvercharge);
        }

        double overextendEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityOverextend);
        if (overextendEfficiency > 0 && !attacker.getFoodStats().needFood()) {
            range += overextendEfficiency;
        }

        return range;
    }

    private void spawnGroundParticles(World world, Vector3d origin, Vector3d direction, double yaw, double range) {
        Random rand = world.rand;

        BlockState originState = world.getBlockState(new BlockPos(origin));
        ((ServerWorld) world).spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, originState),
                origin.getX(), origin.getY(), origin.getZ(),
                8, 0, rand.nextGaussian() * 0.1, 0, 0.1);
        world.playSound(null, new BlockPos(origin), originState.getSoundType().getBreakSound(), SoundCategory.PLAYERS, 1.5f, 0.5f);

        int bound = (int) Math.ceil(range / 2);

        BlockPos center = new BlockPos(origin.add(direction.scale(range / 2)));
        origin = origin.add(direction.scale(-1));
        BlockPos.Mutable targetPos = new BlockPos.Mutable(0, 0, 0);
        for (int x = -bound; x <= bound; x++) {
            for (int z = -bound; z <= bound; z++) {
                targetPos.setAndOffset(center, x, 0, z);
                if (compareAngle(origin, targetPos, yaw)) {
                    for (int y = -2; y < 2; y++) {
                        targetPos.setAndOffset(center, x, y, z);
                        BlockState targetState = world.getBlockState(targetPos);
                        if (targetState.isSolid() && !world.getBlockState(targetPos.up()).isSolid()) {
                            targetPos.down();

                            double distance = targetPos.distanceSq(origin.x, origin.y, origin.z, true);

                            if (distance < range * range) {
                                double yOffset = targetState.getShape(world, targetPos).getBoundingBox().maxY;
                                BlockPos particlePos = targetPos.toImmutable();

                                ServerScheduler.schedule(particlePos.manhattanDistance(new BlockPos(origin)) - 3, () -> {
                                    ((ServerWorld) world).spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, targetState),
                                            particlePos.getX() + 0.5, particlePos.getY() + yOffset, particlePos.getZ() + 0.5,
                                            3, 0, rand.nextGaussian() * 0.1, 0, 0.1);

                                    if (rand.nextFloat() < 0.3f) {
                                        world.playSound(null, particlePos, targetState.getSoundType().getFallSound(), SoundCategory.PLAYERS, 1, 0.5f);
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

    private boolean inRange(Vector3d origin, Entity entity, double originYaw, double range) {
        if (origin.isWithinDistanceOf(entity.getPositionVec(), range)) {
            Vector3d direction = entity.getPositionVec().subtract(origin);
            double entityYaw = MathHelper.atan2(direction.x, direction.z);
            double yawDiff = Math.abs((originYaw - entityYaw + 3 * Math.PI) % (Math.PI * 2) - Math.PI);

            return yawDiff < Math.PI / 6;
        }
        return false;
    }

    private boolean compareAngle(Vector3d originPos, BlockPos.Mutable offsetPos, double originYaw) {
        Vector3d direction = Vector3d.copyCenteredHorizontally(offsetPos).subtract(originPos);
        double offsetYaw = MathHelper.atan2(direction.getX(), direction.getZ());

        double yawDiff = Math.abs((originYaw - offsetYaw + 3 * Math.PI) % (Math.PI * 2) - Math.PI);
        return yawDiff < Math.PI / 6;
    }

    private static void groundSlamEntity(PlayerEntity attacker, LivingEntity target, ItemModularHandheld item, ItemStack itemStack, Vector3d origin,
            double damageMultiplier, int slowDuration, double momentumEfficiency, int revengeLevel) {
        ServerScheduler.schedule(target.getPosition().manhattanDistance(new BlockPos(origin)) - 3, () -> {
            float knockback = momentumEfficiency > 0 ? 0.1f : 0.5f;

            AbilityUseResult result = item.hitEntity(itemStack, attacker, target, damageMultiplier, knockback, knockback);

            if (momentumEfficiency > 0) {
                target.getEntityWorld().playSound(null, target.getPosition(), SoundEvents.ENTITY_GENERIC_BIG_FALL, SoundCategory.PLAYERS, 1, 0.7f);
            } else {
                target.getEntityWorld().playSound(null, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 1, 0.9f);
            }

            if (result != AbilityUseResult.fail) {
                if (slowDuration > 0) {
                    target.addPotionEffect(new EffectInstance(Effects.SLOWNESS, slowDuration, 1, false, true));
                }

                if (momentumEfficiency > 0) {
                    double velocity = momentumEfficiency;
                    velocity *= 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
                    target.addVelocity(0, velocity, 0);
                    target.addPotionEffect(new EffectInstance(StunPotionEffect.instance, 40, 0, false, false));
                }

                if (revengeLevel > 0 && RevengeTracker.canRevenge(attacker, target)) {
                    target.addPotionEffect(new EffectInstance(StunPotionEffect.instance, revengeLevel, 0, false, false));
                    RevengeTracker.removeEnemySynced((ServerPlayerEntity) attacker, target);
                }
            }

            if (result == AbilityUseResult.crit) {
                Random rand = target.getRNG();
                CastOptional.cast(target.world, ServerWorld.class).ifPresent(world ->
                        world.spawnParticle(ParticleTypes.CRIT,
                                target.getPosX(), target.getPosY(), target.getPosZ(), 10,
                                rand.nextGaussian() * 0.3, rand.nextGaussian() * 0.5, rand.nextGaussian() * 0.3, 0.1f));
            }
        });
    }
}
