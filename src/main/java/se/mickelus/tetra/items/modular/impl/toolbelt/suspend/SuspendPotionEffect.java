package se.mickelus.tetra.items.modular.impl.toolbelt.suspend;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeMod;

public class SuspendPotionEffect extends Effect {
    public static SuspendPotionEffect instance;

    public SuspendPotionEffect() {
        super(EffectType.BENEFICIAL, 0x006600);
        setRegistryName("suspended");

        addAttributesModifier(ForgeMod.ENTITY_GRAVITY.get(), "07607dcd-4ee5-42b1-bc39-90a7bf06b4b5", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);

        instance = this;
    }

    @Override
    public void performEffect(LivingEntity entity, int amplifier) {
        entity.fallDistance = 0;
        if (entity.isOnGround()) {
            entity.removePotionEffect(this);
        } else {
            Vector3d motion = entity.getMotion();
            double dy = motion.y;
            if (entity.isCrouching()) {
                entity.setMotion(motion.x, Math.max(-0.3, dy - 0.05), motion.z);
            } else {
                entity.setMotion(motion.x, Math.abs(dy) > 0.02 ? dy * 0.9 : 0, motion.z);
            }

            EffectInstance effectInstance = entity.getActivePotionEffect(this);
            if (effectInstance != null && effectInstance.getDuration() < 20) {
                if (SuspendEffect.canSuspend((PlayerEntity) entity)) {
                    entity.addPotionEffect(new EffectInstance(SuspendPotionEffect.instance, 100, 0, false, false));
                } else {
                    entity.removePotionEffect(this);
                }
            }
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
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
