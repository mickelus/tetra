package se.mickelus.tetra.items.modular.impl.toolbelt.suspend;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

public class SuspendPotionEffect extends MobEffect {
    public static SuspendPotionEffect instance;

    public SuspendPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x006600);
        setRegistryName("suspended");

        addAttributeModifier(ForgeMod.ENTITY_GRAVITY.get(), "07607dcd-4ee5-42b1-bc39-90a7bf06b4b5", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);

        instance = this;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.fallDistance = 0;
        if (entity.isOnGround()) {
            entity.removeEffect(this);
        } else {
            Vec3 motion = entity.getDeltaMovement();
            double dy = motion.y;
            if (entity.isCrouching()) {
                entity.setDeltaMovement(motion.x, Math.max(-0.3, dy - 0.05), motion.z);
            } else {
                entity.setDeltaMovement(motion.x, Math.abs(dy) > 0.02 ? dy * 0.9 : 0, motion.z);
            }

            MobEffectInstance effectInstance = entity.getEffect(this);
            if (effectInstance != null && effectInstance.getDuration() < 20) {
                if (SuspendEffect.canSuspend((Player) entity)) {
                    entity.addEffect(new MobEffectInstance(SuspendPotionEffect.instance, 100, 0, false, false));
                } else {
                    entity.removeEffect(this);
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
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
