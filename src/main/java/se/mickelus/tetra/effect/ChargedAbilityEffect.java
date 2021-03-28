package se.mickelus.tetra.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

import javax.annotation.Nullable;

public abstract class ChargedAbilityEffect {
    protected int chargeTimeFlat;
    protected double chargeTimeSpeedMultiplier;
    protected ItemEffect effect;
    protected TargetRequirement target;

    protected String modelTransform;
    protected UseAction pose = UseAction.NONE;

    ChargedAbilityEffect(int chargeTimeFlat, double chargeTimeSpeedMultiplier, ItemEffect effect, TargetRequirement target) {
        this.chargeTimeFlat = chargeTimeFlat;
        this.chargeTimeSpeedMultiplier = chargeTimeSpeedMultiplier;
        this.effect = effect;
        this.target = target;
    }

    ChargedAbilityEffect(int chargeTimeFlat, double chargeTimeSpeedMultiplier, ItemEffect effect, TargetRequirement target, UseAction pose, String modelTransform) {
        this(chargeTimeFlat, chargeTimeSpeedMultiplier, effect, target);

        this.pose = pose;
        this.modelTransform = modelTransform;
    }

    public boolean isAvailable(ItemModularHandheld item, ItemStack itemStack) {
        return item.getEffectLevel(itemStack, effect) > 0;
    }

    public boolean canCharge(ItemModularHandheld item, ItemStack itemStack) {
        return isAvailable(item, itemStack);
    }

    public int getChargeTime(ItemModularHandheld item, ItemStack itemStack) {
        return chargeTimeFlat + (chargeTimeSpeedMultiplier != 0 ? (int) (item.getCooldownBase(itemStack) * chargeTimeSpeedMultiplier) : 0);
    }

    public boolean canPerform(PlayerEntity attacker, ItemModularHandheld item, ItemStack itemStack, @Nullable LivingEntity target, @Nullable BlockPos targetPos, int chargedTicks) {
        return isAvailable(item, itemStack) && chargedTicks >= getChargeTime(item, itemStack) && hasRequiredTarget(target, targetPos);
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

    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, @Nullable LivingEntity target, @Nullable BlockPos targetPos, int chargedTicks) {
        if (target != null) {
            perform(attacker, hand, item, itemStack, target, chargedTicks);
        } else if (targetPos != null) {
            perform(attacker, hand, item, itemStack, targetPos, chargedTicks);
        } else {
            perform(attacker, hand, item, itemStack, chargedTicks);
        }
    }

    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, LivingEntity target, int chargedTicks) {

    }

    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, BlockPos targetPos, int chargedTicks) {

    }

    public void perform(PlayerEntity attacker, Hand hand, ItemModularHandheld item, ItemStack itemStack, int chargedTicks) {

    }

    public UseAction getPose() {
        return pose;
    }

    public String getModelTransform() {
        return modelTransform;
    }

    enum TargetRequirement {
        entity,
        block,
        either,
        none
    }
}
