package se.mickelus.tetra.effect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.effect.potion.ExhaustedPotionEffect;
import se.mickelus.tetra.effect.potion.StunPotionEffect;
import se.mickelus.tetra.effect.revenge.RevengeTracker;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

public class LungeEffect extends ChargedAbilityEffect {
    private static Cache<Integer, LungeData> activeCache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    public static final LungeEffect instance = new LungeEffect();

    LungeEffect() {
        super(5, 0.5f, 60, 6.5, ItemEffect.lunge, TargetRequirement.none, UseAction.SPEAR, "raised");
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
    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack,
            @Nullable LivingEntity target, @Nullable BlockPos targetPos,@Nullable Vector3d hitVec, int chargedTicks) {
        if (attacker.isOnGround()) {
            float damageMultiplierOffset = 0;
            float strength = 1 + EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, itemStack) * 0.5f;
            Vector3d lookVector = attacker.getLookVec();
            double verticalVelocityFactor = 0.8;
            float hitCooldown = 0.7f;
            float exhaustDuration = 0;
            double echoStrength = 0;

            if (canOvercharge(item, itemStack)) {
                int overcharge = getOverchargeBonus(item, itemStack, chargedTicks);
                strength += overcharge * item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge) / 100f;
                damageMultiplierOffset += item.getEffectEfficiency(itemStack, ItemEffect.abilityOvercharge) / 100f;
            }

            double comboEfficiency = item.getEffectEfficiency(itemStack, ItemEffect.abilityCombo);
            if (comboEfficiency > 0) {
                hitCooldown -= comboEfficiency * ComboPoints.get(attacker) / 100;
            }

            int momentumLevel = item.getEffectLevel(itemStack, ItemEffect.abilityMomentum);
            if (momentumLevel > 0) {
                verticalVelocityFactor = 1.2;
            }

            int overextendLevel = item.getEffectLevel(itemStack, ItemEffect.abilityOverextend);
            if (overextendLevel > 0 && !attacker.getFoodStats().needFood()) {
                damageMultiplierOffset += overextendLevel / 100d;
                strength += item.getEffectEfficiency(itemStack, ItemEffect.abilityOverextend);
                verticalVelocityFactor += 0.1;
            }

            int exhilarateLevel = item.getEffectLevel(itemStack, ItemEffect.abilityExhilaration);
            if (exhilarateLevel > 0) {
                damageMultiplierOffset += exhilarateLevel / 100f;
                strength += item.getEffectEfficiency(itemStack, ItemEffect.abilityExhilaration);
                exhaustDuration += 15;
            }

            int echoLevel = item.getEffectLevel(itemStack, ItemEffect.abilityEcho);
            if (echoLevel > 0) {
                echoStrength = item.getEffectEfficiency(itemStack, ItemEffect.abilityEcho);
            }

            if (isDefensive(item, itemStack, hand)) {
                lookVector = lookVector.mul(-1.2, 0, -1.2).add(0, 0.4, 0);
            } else {
                activeCache.put(getIdentifier(attacker), new LungeData(itemStack, damageMultiplierOffset, hitCooldown, exhaustDuration, echoLevel, echoStrength));
            }

            // current velocity projected onto the look vector
            attacker.setMotion(lookVector.scale(attacker.getMotion().dotProduct(lookVector) / lookVector.dotProduct(lookVector)));

            attacker.addVelocity(
                    lookVector.x * strength,
                    MathHelper.clamp(lookVector.y * strength * verticalVelocityFactor + 0.3, 0.3, verticalVelocityFactor),
                    lookVector.z * strength);
            attacker.velocityChanged = true;

            attacker.move(MoverType.SELF, new Vector3d(0, 0.4, 0));

            attacker.addExhaustion(overextendLevel > 0 ? 6 : 1);
            attacker.getCooldownTracker().setCooldown(item, getCooldown(item, itemStack));

            attacker.getEntityWorld().playSound(attacker, new BlockPos(attacker.getPositionVec().add(attacker.getMotion())), SoundEvents.UI_TOAST_IN,
                    SoundCategory.PLAYERS, 1, 1.3f);

        }

        if (ComboPoints.canSpend(item, itemStack)) {
            ComboPoints.reset(attacker);
        }
    }

    public static void onPlayerTick(PlayerEntity player) {
        LungeData data = activeCache.getIfPresent(getIdentifier(player));
        if (data != null && !player.isPassenger()) {
            if (!player.isOnGround()) {
                AxisAlignedBB axisalignedbb = player.getBoundingBox().grow(0.2, 0, 0.2).offset(player.getMotion());

                player.world.getEntitiesWithinAABB(LivingEntity.class, axisalignedbb).stream()
                        .filter(Entity::isAlive)
                        .filter(Entity::canBeCollidedWith)
                        .filter(Entity::canBeAttackedWithItem)
                        .filter(entity -> !player.equals(entity))
                        .findAny()
                        .ifPresent(entity -> onEntityImpact(player, entity, data));
            } else {
                activeCache.invalidate(getIdentifier(player));

                if (data.exhaustDuration > 0) {
                    player.addPotionEffect(new EffectInstance(ExhaustedPotionEffect.instance, (int) (data.exhaustDuration * 20), 4, false, true));
                }
            }
        }
    }

    private static void onEntityImpact(PlayerEntity player, LivingEntity target, LungeData data) {
        ItemStack itemStack = data.itemStack;
        ItemModularHandheld item = CastOptional.cast(itemStack.getItem(), ItemModularHandheld.class).orElse(null);

        Vector3d bounceVector = player.getPositionVec().subtract(target.getPositionVec()).normalize().scale(0.1f);
        player.setMotion(bounceVector);
        player.velocityChanged = true;

        float cooldownMultiplier = data.hitCooldown;

        int momentumLevel = item.getEffectLevel(itemStack, ItemEffect.abilityMomentum);

        if (RevengeTracker.canRevenge(player) && RevengeTracker.canRevenge(player, target)) {
            cooldownMultiplier = 0;
            RevengeTracker.removeEnemy(player, target);
        }

        if (!player.world.isRemote) {
            double bonusDamage = 0;

            if (momentumLevel > 0) {
                bonusDamage = Math.min(momentumLevel, player.fallDistance);
            }

            int lungeLevel = item.getEffectLevel(itemStack, ItemEffect.lunge);
            AbilityUseResult result = item.hitEntity(itemStack, player, target, data.damageMultiplierOffset + lungeLevel / 100f, bonusDamage,
                    0.5f, 0.5f);

            if (result != AbilityUseResult.fail) {
                if (momentumLevel > 0) {
                    int duration = 10 + (int) (Math.min(momentumLevel, player.fallDistance) * item.getEffectEfficiency(itemStack, ItemEffect.abilityMomentum) * 20);
                    target.addPotionEffect(new EffectInstance(StunPotionEffect.instance, duration, 0, false, false));
                    spawnMomentumParticles(target, bonusDamage);
                }
            }

            target.getEntityWorld().playSound(null, target.getPosition(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.PLAYERS, 1, 0.8f);
            player.swing(Hand.MAIN_HAND, true);
        }

        if (momentumLevel > 0) {
            player.fallDistance = Math.max(0, player.fallDistance - momentumLevel);
        }

        item.tickProgression(player, itemStack, 2);
        item.applyDamage(2, itemStack, player);

        player.getCooldownTracker().setCooldown(item, (int) (instance.getCooldown(item, itemStack) * cooldownMultiplier));

        activeCache.invalidate(getIdentifier(player));
    }

    private static void spawnMomentumParticles(LivingEntity target, double bonus) {
        BlockPos pos = new BlockPos(target.getPosX(), target.getPosY() - 0.2, target.getPosZ());
        BlockState blockState = target.world.getBlockState(pos);

        if (!blockState.isAir(target.world, pos)) {
            ((ServerWorld)target.world).spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, blockState), target.getPosX(), target.getPosY(),
                    target.getPosZ(), (int) (bonus * 8) + 20, 0.0D, 0.0D, 0.0D, 0.15);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void onRightClick(ClientPlayerEntity player) {
        LungeData data = activeCache.getIfPresent(getIdentifier(player));
        if (data != null && data.echoCount > 0) {
            PacketHandler.sendToServer(new LungeEchoPacket());
            echo(player, data, false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void onJump(ClientPlayerEntity player) {
        LungeData data = activeCache.getIfPresent(getIdentifier(player));
        if (data != null && data.echoCount > 0) {
            PacketHandler.sendToServer(new LungeEchoPacket(true));
            echo(player, data, true);
        }
    }

    public static void receiveEchoPacket(PlayerEntity player, boolean isVertical) {
        LungeData data = activeCache.getIfPresent(getIdentifier(player));
        if (data != null && data.echoCount > 0) {
            echo(player, data, isVertical);
        }
    }

    public static void echo(PlayerEntity entity, LungeData data, boolean isVertical) {
        Vector3d lookVector = entity.getLookVec();
        entity.setMotion(lookVector.scale(entity.getMotion().dotProduct(lookVector) / lookVector.dotProduct(lookVector)));

        double strength = data.echoStrength;

        if (isVertical) {
            strength *= 0.3;
        }

        entity.addVelocity(
                lookVector.x * strength,
                lookVector.y * strength,
                lookVector.z * strength);

        if (entity.isCrouching()) {
            entity.setMotion(entity.getMotion().scale(-0.8));
        }

        if (isVertical) {
            Vector3d motion = entity.getMotion();
            double vertical = motion.y > 0 ? motion.y + strength * 0.5 : strength * 0.5;
            entity.setMotion(motion.x, vertical, motion.z);
        }

        if (entity.getMotion().y > 0) {
            entity.fallDistance = 0;
        }

        entity.velocityChanged = true;

        entity.getEntityWorld().playSound(entity, new BlockPos(entity.getPositionVec().add(entity.getMotion())), SoundEvents.UI_TOAST_IN,
                SoundCategory.PLAYERS, 1, 1.3f);

        data.echoCount--;
    }

    private static int getIdentifier(PlayerEntity entity) {
        return entity.world.isRemote ? -entity.getEntityId() : entity.getEntityId();
    }

    static class LungeData {
        ItemStack itemStack;
        float damageMultiplierOffset;
        float hitCooldown;
        float exhaustDuration;
        int echoCount;
        double echoStrength;

        public LungeData(ItemStack itemStack, float damageMultiplierOffset, float hitCooldown, float exhaustDuration, int echoCount, double echoStrength) {
            this.itemStack = itemStack;
            this.damageMultiplierOffset = damageMultiplierOffset;
            this.hitCooldown = hitCooldown;
            this.exhaustDuration = exhaustDuration;
            this.echoCount = echoCount;
            this.echoStrength = echoStrength;
        }
    }
}
