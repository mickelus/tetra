package se.mickelus.tetra.effect.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;

public class BleedingPotionEffect extends Effect {
    public static BleedingPotionEffect instance;
    public BleedingPotionEffect() {
        super(EffectType.HARMFUL, 0x880000);

        setRegistryName("bleeding");

        instance = this;
    }

    @Override
    public void performEffect(LivingEntity entity, int amplifier) {
        entity.attackEntityFrom(DamageSource.GENERIC, amplifier);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public boolean shouldRender(EffectInstance effect) {
        return false;
    }

    @Override
    public boolean shouldRenderHUD(EffectInstance effect) {
        return false;
    }
}
