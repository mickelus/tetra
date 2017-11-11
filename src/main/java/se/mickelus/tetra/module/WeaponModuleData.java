package se.mickelus.tetra.module;

public class WeaponModuleData extends ModuleData {

    public float damage = 0;
    public float damageModifier = 1;

    public float attackSpeed = 0;
    public float attackSpeedModifier = 1;

    public WeaponModuleData(final String key, final String material, final int durability, final float durabilityMultiplier,
            final int integrity, final float integrityMultiplier, final float damage, final float damageModifier,
            final float attackSpeed, final float attackSpeedModifier,
            final int glyphTint, final int glyphTextureX, final int glyphTextureY) {

        super(key, material, durability, durabilityMultiplier, integrity, integrityMultiplier, glyphTint, glyphTextureX, glyphTextureY);
        this.damage = damage;
        this.damageModifier = damageModifier;

        this.attackSpeed = attackSpeed;
        this.attackSpeedModifier = attackSpeedModifier;
    }

    public WeaponModuleData(final String key, final String material, final int durability, final int integrity,
            final float damage, final float attackSpeed,
            final int glyphTint, final int glyphTextureX, final int glyphTextureY) {

        super(key, material, durability, integrity, glyphTint, glyphTextureX, glyphTextureY);
        this.damage = damage;
        this.attackSpeed = attackSpeed;
    }
}
