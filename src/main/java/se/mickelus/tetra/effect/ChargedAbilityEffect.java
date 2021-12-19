package se.mickelus.tetra.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
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
    protected UseAnim useAction;

    public ChargedAbilityEffect(int chargeTimeFlat, double chargeTimeSpeedMultiplier, int cooldownFlat, double cooldownSpeedMultiplier,
            ItemEffect effect, TargetRequirement target, UseAnim useAction) {
        this.chargeTimeFlat = chargeTimeFlat;
        this.chargeTimeSpeedMultiplier = chargeTimeSpeedMultiplier;

        this.cooldownFlat = cooldownFlat;
        this.cooldownSpeedMultiplier = cooldownSpeedMultiplier;

        this.effect = effect;
        this.target = target;

        this.useAction = useAction;
    }

    public ChargedAbilityEffect(int chargeTimeFlat, double chargeTimeSpeedMultiplier, int cooldownFlat, double cooldownSpeedMultiplier,
            ItemEffect effect, TargetRequirement target, UseAnim pose, String modelTransform) {
        this(chargeTimeFlat, chargeTimeSpeedMultiplier, cooldownFlat, cooldownSpeedMultiplier, effect, target, pose);

        this.modelTransform = modelTransform;
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

    public boolean isAvailable(ItemModularHandheld item, ItemStack itemStack) {
        return item.getEffectLevel(itemStack, effect) > 0;
    }

    public boolean canCharge(ItemModularHandheld item, ItemStack itemStack) {
        return isAvailable(item, itemStack);
    }

    public boolean canOvercharge(ItemModularHandheld item, ItemStack itemStack) {
        return isAvailable(item, itemStack) && item.getEffectLevel(itemStack, ItemEffect.abilityOvercharge) > 0;
    }

    public double getOverchargeProgress(ItemModularHandheld item, ItemStack itemStack, int chargedTicks) {
        int chargeTime = getChargeTime(item, itemStack);
        return getOverchargeProgress(chargedTicks * 1f / chargeTime - 1);
    }

    public int getOverchargeBonus(ItemModularHandheld item, ItemStack itemStack, int chargedTicks) {
        return (int) Mth.clamp(getOverchargeProgress(item, itemStack, chargedTicks), 0, 3);
    }

    public int getChargeTime(ItemModularHandheld item, ItemStack itemStack) {
        return (int) ((chargeTimeFlat + (chargeTimeSpeedMultiplier != 0 ? (int) (item.getCooldownBase(itemStack) * 20 * chargeTimeSpeedMultiplier) : 0))
                * getSpeedBonusMultiplier(item, itemStack));
    }

    public int getChargeTime(Player attacker, ItemModularHandheld item, ItemStack itemStack) {
        return getChargeTime(item, itemStack);
    }

    public int getCooldown(ItemModularHandheld item, ItemStack itemStack) {
        return (int) ((cooldownFlat + (cooldownSpeedMultiplier != 0 ? (int) (item.getCooldownBase(itemStack) * 20 * cooldownSpeedMultiplier) : 0))
                * getSpeedBonusMultiplier(item, itemStack));
    }

    public float getSpeedBonusMultiplier(ItemModularHandheld item, ItemStack itemStack) {
        return (100 - item.getEffectLevel(itemStack, ItemEffect.abilitySpeed)) / 100f;
    }

    public boolean isDefensive(ItemModularHandheld item, ItemStack itemStack, InteractionHand hand) {
        return hand == InteractionHand.OFF_HAND && item.getEffectLevel(itemStack, ItemEffect.abilityDefensive) > 0;
    }

    public boolean canPerform(Player attacker, ItemModularHandheld item, ItemStack itemStack, @Nullable LivingEntity target, @Nullable BlockPos targetPos, int chargedTicks) {
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

    public void perform(Player attacker, InteractionHand hand, ItemModularHandheld item, ItemStack itemStack,
            @Nullable LivingEntity target, @Nullable BlockPos targetPos, @Nullable Vec3 hitVec, int chargedTicks) {
        // hitvec should only be null if there is no target pos or entity
        if (target != null) {
            perform(attacker, hand, item, itemStack, target, hitVec, chargedTicks);
        } else if (targetPos != null) {
            perform(attacker, hand, item, itemStack, targetPos, hitVec, chargedTicks);
        } else {
            perform(attacker, hand, item, itemStack, chargedTicks);
        }
    }

    public void perform(Player attacker, InteractionHand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, Vec3 hitVec, int chargedTicks) {

    }

    public void perform(Player attacker, InteractionHand hand, ItemModularHandheld item, ItemStack itemStack, BlockPos targetPos, Vec3 hitVec, int chargedTicks) {

    }

    public void perform(Player attacker, InteractionHand hand, ItemModularHandheld item, ItemStack itemStack, int chargedTicks) {

    }

    public UseAnim getPose() {
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
