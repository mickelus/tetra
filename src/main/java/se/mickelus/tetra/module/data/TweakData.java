package se.mickelus.tetra.module.data;

import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.module.ItemEffect;

public class TweakData {
    public String variant;
    public String improvement;

    public String key;
    public int steps;

    private ModuleVariantData baseStats = new ModuleVariantData();
    private ModuleVariantData stepStats;


    public float getDamage(int step) {
        return baseStats.damage + step * stepStats.damage;
    }

    public float getDamageMultiplier(int step) {
        return baseStats.damageMultiplier + step * (stepStats.damageMultiplier - 1);
    }

    public float getAttackSpeed(int step) {
        return baseStats.attackSpeed + step * stepStats.attackSpeed;
    }

    public float getAttackSpeedMultiplier(int step) {
        return baseStats.attackSpeedMultiplier + step * (stepStats.attackSpeedMultiplier -1);
    }

    public float getRange(int step) {
        return baseStats.range + step * stepStats.range;
    }

    public int getDurability(int step) {
        return baseStats.durability + step * stepStats.durability;
    }

    public float getDurabilityMultiplier(int step) {
        return baseStats.durabilityMultiplier + step * (stepStats.durabilityMultiplier - 1);
    }

    public float getEffectEfficiency(ItemEffect effect, int step) {
        return baseStats.effects.getEfficiency(effect) + step * stepStats.effects.getEfficiency(effect);
    }

    public float getCapabilityEfficiency(Capability capability, int step) {
        return baseStats.capabilities.getEfficiency(capability) + step * stepStats.capabilities.getEfficiency(capability);
    }

    public int getMagicCapacity(int step) {
        return baseStats.magicCapacity + step * stepStats.magicCapacity;
    }
}
