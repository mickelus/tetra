package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import javax.annotation.Nullable;

public abstract class ChargedAbilityEffect {
    protected int chargeTimeFlat;
    protected double chargeTimeSpeedMultiplier;

    protected int cooldownFlat;
    protected double cooldownSpeedMultiplier;

    protected ItemEffect effect;
    protected TargetRequirement target;

    protected String modelTransform;
    protected UseAction useAction;

    public ChargedAbilityEffect(int chargeTimeFlat, double chargeTimeSpeedMultiplier, int cooldownFlat, double cooldownSpeedMultiplier,
            ItemEffect effect, TargetRequirement target, UseAction useAction) {
        this.chargeTimeFlat = chargeTimeFlat;
        this.chargeTimeSpeedMultiplier = chargeTimeSpeedMultiplier;

        this.cooldownFlat = cooldownFlat;
        this.cooldownSpeedMultiplier = cooldownSpeedMultiplier;

        this.effect = effect;
        this.target = target;

        this.useAction = useAction;
    }

    public ChargedAbilityEffect(int chargeTimeFlat, double chargeTimeSpeedMultiplier, int cooldownFlat, double cooldownSpeedMultiplier,
            ItemEffect effect, TargetRequirement target, UseAction pose, String modelTransform) {
        this(chargeTimeFlat, chargeTimeSpeedMultiplier, cooldownFlat, cooldownSpeedMultiplier, effect, target, pose);

        this.modelTransform = modelTransform;
    }

    public boolean isAvailable(ItemModularHandheld item, ItemStack itemStack) {
        return item.getEffectLevel(itemStack, effect) > 0;
    }

    public boolean canCharge(ItemModularHandheld item, ItemStack itemStack) {
        return isAvailable(item, itemStack);
    }

    public boolean canOvercharge(ItemModularHandheld item, ItemStack itemStack) {
        return isAvailable(item, itemStack) && item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge) > 0;
    }

    public static double getOverchargeProgress(float progress) {
        if (progress > 1.5) {
            return 0.75 * progress + 0.875;
        }
        if (progress > 0.5) {
            return progress + 0.5;
        }

        return 2 * progress;
    }

    public double getOverchargeProgress(ItemModularHandheld item, ItemStack itemStack, int chargedTicks) {
        int chargeTime = getChargeTime(item, itemStack);
        return getOverchargeProgress(chargedTicks * 1f / chargeTime - 1);
    }

    public int getOverchargeBonus(ItemModularHandheld item, ItemStack itemStack, int chargedTicks) {
        return (int) MathHelper.clamp(getOverchargeProgress(item, itemStack, chargedTicks), 0, 3);
    }

    public int getChargeTime(ItemModularHandheld item, ItemStack itemStack) {
        return (int) ((chargeTimeFlat + (chargeTimeSpeedMultiplier != 0 ? (int) (item.getCooldownBase(itemStack) * 20 * chargeTimeSpeedMultiplier) : 0))
                * getSpeedBonusMultiplier(item, itemStack));
    }

    public int getChargeTime(PlayerEntity attacker, ItemModularHandheld item, ItemStack itemStack) {
        return getChargeTime(item, itemStack);
    }

    public int getCooldown(ItemModularHandheld item, ItemStack itemStack) {
        return (int) ((cooldownFlat + (cooldownSpeedMultiplier != 0 ? (int) (item.getCooldownBase(itemStack) * 20 * cooldownSpeedMultiplier) : 0))
                * getSpeedBonusMultiplier(item, itemStack));
    }

    public float getSpeedBonusMultiplier(ItemModularHandheld item, ItemStack itemStack) {
        return (100 - item.getEffectLevel(itemStack, ItemEffect.abilitySpeed)) / 100f;
    }

    public boolean isDefensive(ItemModularHandheld item, ItemStack itemStack, Hand hand) {
        return hand == Hand.OFF_HAND && item.getEffectLevel(itemStack, ItemEffect.abilityDefensive) > 0;
    }

    public boolean canPerform(PlayerEntity attacker, ItemModularHandheld item, ItemStack itemStack, @Nullable LivingEntity target, @Nullable BlockPos targetPos, int chargedTicks) {
        return isAvailable(item, itemStack) && chargedTicks >= getChargeTime(attacker, item, itemStack) && hasRequiredTarget(target, targetPos);
    }

    boolean hasRequiredTarget(@Nullable LivingEntity target, @Nullable BlockPos targetPos) {
        switch (this.target) {
            case entity:
                return target != null;
            case block:
                return targetPos != null;
            case either:
                return target != null || targetPos != null;
            case none:
                return true;
        }
        return true;
    }

    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack,
            @Nullable LivingEntity target, @Nullable BlockPos targetPos, @Nullable Vector3d hitVec, int chargedTicks) {
        // hitvec should only be null if there is no target pos or entity
        if (target != null) {
            perform(attacker, hand, item, itemStack, target, hitVec, chargedTicks);
        } else if (targetPos != null) {
            perform(attacker, hand, item, itemStack, targetPos, hitVec, chargedTicks);
        } else {
            perform(attacker, hand, item, itemStack, chargedTicks);
        }
    }

    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vector3d hitVec, int chargedTicks) {

    }

    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, BlockPos targetPos, Vector3d hitVec, int chargedTicks) {

    }

    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, int chargedTicks) {

    }

    public UseAction getPose() {
        return useAction;
    }

    public String getModelTransform() {
        return modelTransform;
    }

    public enum TargetRequirement {
        entity,
        block,
        either,
        none
    }
}
