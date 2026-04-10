package se.mickelus.tetra.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class ApplyUsageEffectsEvent extends Event {
    private LivingEntity usingEntity;
    private ItemStack itemStack;
    private double originalPositiveMultiplier;
    private double positiveMultiplier;
    private double originalNegativeMultiplier;
    private double negativeMultiplier;

    public ApplyUsageEffectsEvent(LivingEntity usingEntity, ItemStack itemStack, double multiplier) {
        this.usingEntity = usingEntity;
        this.itemStack = itemStack;
        this.originalPositiveMultiplier = multiplier;
        this.positiveMultiplier = multiplier;
        this.originalNegativeMultiplier = multiplier;
        this.negativeMultiplier = multiplier;
    }

    public LivingEntity getUsingEntity() {
        return usingEntity;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public double getOriginalPositiveMultiplier() {
        return originalPositiveMultiplier;
    }

    public double getOriginalNegativeMultiplier() {
        return originalNegativeMultiplier;
    }

    public double getPositiveMultiplier() {
        return positiveMultiplier;
    }

    public void setPositiveMultiplier(double positiveMultiplier) {
        this.positiveMultiplier = positiveMultiplier;
    }

    public double getNegativeMultiplier() {
        return negativeMultiplier;
    }

    public void setNegativeMultiplier(double negativeMultiplier) {
        this.negativeMultiplier = negativeMultiplier;
    }
}
