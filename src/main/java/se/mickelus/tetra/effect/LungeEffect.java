package se.mickelus.tetra.effect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.effect.potion.ExhaustedPotionEffect;
import se.mickelus.tetra.effect.potion.StunPotionEffect;
import se.mickelus.tetra.effect.revenge.RevengeTracker;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class LungeEffect extends ChargedAbilityEffect {
    private static Cache<Integer, LungeData> activeCache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    public static final LungeEffect instance = new LungeEffect();

    LungeEffect() {
        super(5, 0.5f, 60, 6.5, ItemEffect.lunge, TargetRequirement.none, UseAnim.SPEAR, "raised");
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
    public void perform(Player attacker, InteractionHand hand, ItemModularHandheld item, ItemStack itemStack,
            @Nullable LivingEntity target, @Nullable BlockPos targetPos,@Nullable Vec3 hitVec, int chargedTicks) {
        if (attacker.isOnGround()) {
            float damageMultiplierOffset = 0;
            float strength = 1 + EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, itemStack) * 0.5f;
            Vec3 lookVector = attacker.getLookAngle();
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
            if (overextendLevel > 0 && !attacker.getFoodData().needsFood()) {
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
                lookVector = lookVector.multiply(-1.2, 0, -1.2).add(0, 0.4, 0);
            } else {
                activeCache.put(getIdentifier(attacker), new LungeData(itemStack, damageMultiplierOffset, hitCooldown, exhaustDuration, echoLevel, echoStrength));
            }

            // current velocity projected onto the look vector
            attacker.setDeltaMovement(lookVector.scale(attacker.getDeltaMovement().dot(lookVector) / lookVector.dot(lookVector)));

            attacker.push(
                    lookVector.x * strength,
                    Mth.clamp(lookVector.y * strength * verticalVelocityFactor + 0.3, 0.3, verticalVelocityFactor),
                    lookVector.z * strength);
            attacker.hurtMarked = true;

            attacker.move(MoverType.SELF, new Vec3(0, 0.4, 0));

            attacker.causeFoodExhaustion(overextendLevel > 0 ? 6 : 1);
            attacker.getCooldowns().addCooldown(item, getCooldown(item, itemStack));

            attacker.getCommandSenderWorld().playSound(attacker, new BlockPos(attacker.position().add(attacker.getDeltaMovement())), SoundEvents.UI_TOAST_IN,
                    SoundSource.PLAYERS, 1, 1.3f);

        }

        if (ComboPoints.canSpend(item, itemStack)) {
            ComboPoints.reset(attacker);
        }
    }

    public static void onPlayerTick(Player player) {
        LungeData data = activeCache.getIfPresent(getIdentifier(player));
        if (data != null && !player.isPassenger()) {
            if (!player.isOnGround()) {
                AABB axisalignedbb = player.getBoundingBox().inflate(0.2, 0, 0.2).move(player.getDeltaMovement());

                player.level.getEntitiesOfClass(LivingEntity.class, axisalignedbb).stream()
                        .filter(Entity::isAlive)
                        .filter(Entity::isPickable)
                        .filter(Entity::isAttackable)
                        .filter(entity -> !player.equals(entity))
                        .findAny()
                        .ifPresent(entity -> onEntityImpact(player, entity, data));
            } else {
                activeCache.invalidate(getIdentifier(player));

                if (data.exhaustDuration > 0) {
                    player.addEffect(new MobEffectInstance(ExhaustedPotionEffect.instance, (int) (data.exhaustDuration * 20), 4, false, true));
                }
            }
        }
    }

    private static void onEntityImpact(Player player, LivingEntity target, LungeData data) {
        ItemStack itemStack = data.itemStack;
        ItemModularHandheld item = CastOptional.cast(itemStack.getItem(), ItemModularHandheld.class).orElse(null);

        Vec3 bounceVector = player.position().subtract(target.position()).normalize().scale(0.1f);
        player.setDeltaMovement(bounceVector);
        player.hurtMarked = true;

        float cooldownMultiplier = data.hitCooldown;

        int momentumLevel = item.getEffectLevel(itemStack, ItemEffect.abilityMomentum);

        if (RevengeTracker.canRevenge(player) && RevengeTracker.canRevenge(player, target)) {
            cooldownMultiplier = 0;
            RevengeTracker.removeEnemy(player, target);
        }

        if (!player.level.isClientSide) {
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
                    target.addEffect(new MobEffectInstance(StunPotionEffect.instance, duration, 0, false, false));
                    spawnMomentumParticles(target, bonusDamage);
                }
            }

            target.getCommandSenderWorld().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1, 0.8f);
            player.swing(InteractionHand.MAIN_HAND, true);
        }

        if (momentumLevel > 0) {
            player.fallDistance = Math.max(0, player.fallDistance - momentumLevel);
        }

        item.tickProgression(player, itemStack, 2);
        item.applyDamage(2, itemStack, player);

        player.getCooldowns().addCooldown(item, (int) (instance.getCooldown(item, itemStack) * cooldownMultiplier));

        activeCache.invalidate(getIdentifier(player));
    }

    private static void spawnMomentumParticles(LivingEntity target, double bonus) {
        BlockPos pos = new BlockPos(target.getX(), target.getY() - 0.2, target.getZ());
        BlockState blockState = target.level.getBlockState(pos);

        if (!blockState.isAir(target.level, pos)) {
            ((ServerLevel)target.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState), target.getX(), target.getY(),
                    target.getZ(), (int) (bonus * 8) + 20, 0.0D, 0.0D, 0.0D, 0.15);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void onRightClick(LocalPlayer player) {
        LungeData data = activeCache.getIfPresent(getIdentifier(player));
        if (data != null && data.echoCount > 0) {
            TetraMod.packetHandler.sendToServer(new LungeEchoPacket());
            echo(player, data, false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void onJump(LocalPlayer player) {
        LungeData data = activeCache.getIfPresent(getIdentifier(player));
        if (data != null && data.echoCount > 0) {
            TetraMod.packetHandler.sendToServer(new LungeEchoPacket(true));
            echo(player, data, true);
        }
    }

    public static void receiveEchoPacket(Player player, boolean isVertical) {
        LungeData data = activeCache.getIfPresent(getIdentifier(player));
        if (data != null && data.echoCount > 0) {
            echo(player, data, isVertical);
        }
    }

    public static void echo(Player entity, LungeData data, boolean isVertical) {
        Vec3 lookVector = entity.getLookAngle();
        entity.setDeltaMovement(lookVector.scale(entity.getDeltaMovement().dot(lookVector) / lookVector.dot(lookVector)));

        double strength = data.echoStrength;

        if (isVertical) {
            strength *= 0.3;
        }

        entity.push(
                lookVector.x * strength,
                lookVector.y * strength,
                lookVector.z * strength);

        if (entity.isCrouching()) {
            entity.setDeltaMovement(entity.getDeltaMovement().scale(-0.8));
        }

        if (isVertical) {
            Vec3 motion = entity.getDeltaMovement();
            double vertical = motion.y > 0 ? motion.y + strength * 0.5 : strength * 0.5;
            entity.setDeltaMovement(motion.x, vertical, motion.z);
        }

        if (entity.getDeltaMovement().y > 0) {
            entity.fallDistance = 0;
        }

        entity.hurtMarked = true;

        entity.getCommandSenderWorld().playSound(entity, new BlockPos(entity.position().add(entity.getDeltaMovement())), SoundEvents.UI_TOAST_IN,
                SoundSource.PLAYERS, 1, 1.3f);

        if (!entity.level.isClientSide) {
            Random rand = entity.getRandom();
            ((ServerLevel) entity.level).sendParticles(ParticleTypes.WITCH,
                    entity.getX() + (rand.nextGaussian() - 0.5) * 0.5,
                    entity.getY(),
                    entity.getZ() + (rand.nextGaussian() - 0.5) * 0.5,
                    5, rand.nextFloat() * 0.2, -0.2 + rand.nextFloat() * -0.6, rand.nextFloat() * 0.2, 0);
        }

        data.echoCount--;
    }

    private static int getIdentifier(Player entity) {
        return entity.level.isClientSide ? -entity.getId() : entity.getId();
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
