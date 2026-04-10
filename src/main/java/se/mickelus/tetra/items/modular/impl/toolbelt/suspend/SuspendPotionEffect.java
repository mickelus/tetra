package se.mickelus.tetra.items.modular.impl.toolbelt.suspend;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SuspendPotionEffect extends MobEffect {
    public static final String identifier = "suspended";
    public static SuspendPotionEffect instance;

    public SuspendPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x006600);

        addAttributeModifier(Attributes.GRAVITY, ResourceLocation.fromNamespaceAndPath("tetra", "suspend_entity_gravity"), -1,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        instance = this;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.fallDistance = 0;
        var effect = se.mickelus.tetra.effect.EffectHelper.effectHolder(this);
        if (entity.onGround()) {
            entity.removeEffect(effect);
        } else {
            Vec3 motion = entity.getDeltaMovement();
            double dy = motion.y;
            if (entity.isCrouching()) {
                entity.setDeltaMovement(motion.x, Math.max(-0.3, dy - 0.05), motion.z);
            } else {
                entity.setDeltaMovement(motion.x, Math.abs(dy) > 0.02 ? dy * 0.9 : 0, motion.z);
            }

            MobEffectInstance effectInstance = entity.getEffect(effect);
            if (effectInstance != null && effectInstance.getDuration() < 20) {
                if (entity instanceof Player player && SuspendEffect.canSuspend(player)) {
                    entity.addEffect(new MobEffectInstance(se.mickelus.tetra.effect.EffectHelper.effectHolder(SuspendPotionEffect.instance), 100, 0, false, false));
                } else {
                    entity.removeEffect(effect);
                }
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
