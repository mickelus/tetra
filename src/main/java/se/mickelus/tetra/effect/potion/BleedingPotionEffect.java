package se.mickelus.tetra.effect.potion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.damagesource.DamageSource;

public class BleedingPotionEffect extends MobEffect {
    public static BleedingPotionEffect instance;
    public BleedingPotionEffect() {
        super(MobEffectCategory.HARMFUL, 0x880000);

        setRegistryName("bleeding");

        instance = this;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.hurt(DamageSource.GENERIC, amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public boolean shouldRender(MobEffectInstance effect) {
        return false;
    }

    @Override
    public boolean shouldRenderHUD(MobEffectInstance effect) {
        return false;
    }
}
