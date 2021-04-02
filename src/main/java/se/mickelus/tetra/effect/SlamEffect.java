package se.mickelus.tetra.effect;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
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
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;

import java.util.Random;

public class SlamEffect extends ChargedAbilityEffect {

    public static final SlamEffect instance = new SlamEffect();

    SlamEffect() {
        super(10, 1f, 40, 6, ItemEffect.slam, TargetRequirement.either, UseAction.SPEAR, "raised");
    }


    @Override
    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vector3d hitVec, int chargedTicks) {
        double damageMultiplier = item.getEffectLevel(itemStack, ItemEffect.slam) * 1.5;
        item.hitEntity(itemStack, attacker, target, damageMultiplier, 1f, 1f);

        target.getEntityWorld().playSound(attacker, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 1, 0.7f);

        Random rand = target.getRNG();
        CastOptional.cast(target.world, ServerWorld.class).ifPresent(world ->
                world.spawnParticle(ParticleTypes.CRIT,
                        hitVec.x, hitVec.y, hitVec.z, 10,
                        rand.nextGaussian() * 0.3, rand.nextGaussian() * target.getHeight() * 0.8, rand.nextGaussian() * 0.3, 0.1f));

        attacker.addExhaustion(0.05f);
        attacker.swing(hand, false);
        attacker.getCooldownTracker().setCooldown(item, Math.round(getCooldown(item, itemStack) * 1.5f));

        item.tickProgression(attacker, itemStack, 2);
        item.applyDamage(2, itemStack, attacker);
    }

    @Override
    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, BlockPos targetPos, Vector3d hitVec, int chargedTicks) {
        if (!attacker.world.isRemote) {
            Vector3d direction = hitVec.subtract(attacker.getPositionVec()).mul(1, 0, 1).normalize();
            double yaw = MathHelper.atan2(direction.x, direction.z);

            AxisAlignedBB axisalignedbb = new AxisAlignedBB(hitVec, hitVec).grow(10, 4, 10).offset(direction.scale(4));

            spawnGroundParticles(attacker.world, hitVec, direction, yaw);

            attacker.world.getEntitiesWithinAABB(LivingEntity.class, axisalignedbb).stream()
                    .filter(Entity::isAlive)
                    .filter(Entity::canBeAttackedWithItem)
                    .filter(entity -> !attacker.equals(entity))
                    .filter(entity -> inRange(hitVec, entity, yaw))
                    .forEach(entity -> groundSlamEntity(attacker, entity, item, itemStack, hitVec));
        }

        attacker.addExhaustion(0.05f);
        attacker.swing(hand, false);
        attacker.getCooldownTracker().setCooldown(item, getCooldown(item, itemStack));

        item.tickProgression(attacker, itemStack, 2);
        item.applyDamage(2, itemStack, attacker);
    }

    private void spawnGroundParticles(World world, Vector3d origin, Vector3d direction, double yaw) {
        Random rand = world.rand;

        BlockState originState = world.getBlockState(new BlockPos(origin));
        ((ServerWorld) world).spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, originState),
                origin.getX(), origin.getY(), origin.getZ(),
                8, 0, rand.nextGaussian() * 0.1, 0, 0.1);
        world.playSound(null, new BlockPos(origin), originState.getSoundType().getBreakSound(), SoundCategory.PLAYERS, 1.5f, 0.5f);


        BlockPos center = new BlockPos(origin.add(direction.scale(4)));
        origin = origin.add(direction.scale(-1));
        BlockPos.Mutable targetPos = new BlockPos.Mutable(0, 0, 0);
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                targetPos.setAndOffset(center, x, 0, z);
                if (compareAngle(origin, targetPos, yaw)) {
                    for (int y = -2; y < 2; y++) {
                        targetPos.setAndOffset(center, x, y, z);
                        BlockState targetState = world.getBlockState(targetPos);
                        if (targetState.isSolid() && !world.getBlockState(targetPos.up()).isSolid()) {
                            targetPos.down();

                            double distance = targetPos.distanceSq(origin.x, origin.y, origin.z, true);

                            if (distance < 64) {
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

    private boolean inRange(Vector3d origin, Entity entity, double originYaw) {
        if (origin.isWithinDistanceOf(entity.getPositionVec(), 8)) {
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

    private static void groundSlamEntity(PlayerEntity attacker, LivingEntity target, ItemModularHandheld item, ItemStack itemStack, Vector3d origin) {
        ServerScheduler.schedule(target.getPosition().manhattanDistance(new BlockPos(origin)) - 3, () -> {
            double damageMultiplier = item.getEffectLevel(itemStack, ItemEffect.slam) / 100f;
            boolean wasCrit = item.hitEntity(itemStack, attacker, target, damageMultiplier, 1f, 1f);

            target.getEntityWorld().playSound(null, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 1, 0.7f);

            if (wasCrit) {
                Random rand = target.getRNG();
                CastOptional.cast(target.world, ServerWorld.class).ifPresent(world ->
                        world.spawnParticle(ParticleTypes.CRIT,
                                target.getPosX(), target.getPosY(), target.getPosZ(), 10,
                                rand.nextGaussian() * 0.3, rand.nextGaussian() * 0.5, rand.nextGaussian() * 0.3, 0.1f));
            }
        });
    }
}
