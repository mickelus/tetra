package se.mickelus.tetra.effect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import se.mickelus.tetra.effect.potion.StunPotionEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

public class LungeEffect extends ChargedAbilityEffect {

    private static Cache<Integer, LungeData> activeCache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();

    public static final LungeEffect instance = new LungeEffect();

    LungeEffect() {
        super(5, 0.5f, 40, 6, ItemEffect.lunge, TargetRequirement.none, UseAction.SPEAR, "raised");
    }

    @Override
    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack,
            @Nullable LivingEntity target, @Nullable BlockPos targetPos,@Nullable Vector3d hitVec, int chargedTicks) {
        if (attacker.isOnGround()) {
            float damageMultiplierOffset = 0;
            float strength = 1 + EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, itemStack) * 0.5f;
            Vector3d lookVector = attacker.getLookVec();
            double verticalVelocityFactor = 0.8;

            if (canOvercharge(item, itemStack)) {
                int overcharge = getOverchargeBonus(item, itemStack, chargedTicks);
                strength += overcharge * item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge) / 100f;
                damageMultiplierOffset += item.getEffectEfficiency(itemStack, ItemEffect.abilityOvercharge) / 100f;
            }

            if (isDefensive(item, itemStack, hand)) {
                lookVector = lookVector.mul(-1.2, 0, -1.2).add(0, 0.4, 0);
            } else {
                activeCache.put(getIdentifier(attacker), new LungeData(itemStack, damageMultiplierOffset));
            }

            int momentumLevel = item.getEffectLevel(itemStack, ItemEffect.abilityMomentum);
            if (momentumLevel > 0) {
                verticalVelocityFactor = 1.2;
            }

            // current velocity projected onto the look vector
            attacker.setMotion(lookVector.scale(attacker.getMotion().dotProduct(lookVector) / lookVector.dotProduct(lookVector)));

            attacker.addVelocity(
                    lookVector.x * strength,
                    MathHelper.clamp(lookVector.y * strength * verticalVelocityFactor + 0.3, 0.3, verticalVelocityFactor),
                    lookVector.z * strength);
            attacker.velocityChanged = true;

            attacker.move(MoverType.SELF, new Vector3d(0, 0.4, 0));

            attacker.addExhaustion(0.2f);
//            attacker.getCooldownTracker().setCooldown(item, getCooldown(item, itemStack));

            attacker.getEntityWorld().playSound(attacker, new BlockPos(attacker.getPositionVec().add(attacker.getMotion())), SoundEvents.UI_TOAST_IN,
                    SoundCategory.PLAYERS, 1, 1.3f);

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
            }
        }
    }

    private static void onEntityImpact(PlayerEntity player, LivingEntity target, LungeData data) {
        ItemStack itemStack = data.itemStack;
        ItemModularHandheld item = CastOptional.cast(itemStack.getItem(), ItemModularHandheld.class).orElse(null);

        Vector3d bounceVector = player.getPositionVec().subtract(target.getPositionVec()).normalize().scale(0.1f);
        player.setMotion(bounceVector);
        player.velocityChanged = true;

        int momentumLevel = item.getEffectLevel(itemStack, ItemEffect.abilityMomentum);

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

        player.getCooldownTracker().setCooldown(item, (int) (instance.getCooldown(item, itemStack) * 0.7));

        activeCache.invalidate(getIdentifier(player));
    }

    private static int getIdentifier(PlayerEntity entity) {
        return entity.world.isRemote ? -entity.getEntityId() : entity.getEntityId();
    }

    static class LungeData {
        ItemStack itemStack;
        float damageMultiplierOffset;

        public LungeData(ItemStack itemStack, float damageMultiplierOffset) {
            this.itemStack = itemStack;
            this.damageMultiplierOffset = damageMultiplierOffset;
        }
    }
}
