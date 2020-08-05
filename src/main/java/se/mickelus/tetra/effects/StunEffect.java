package se.mickelus.tetra.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.vector.Vector3d;

public class StunEffect extends Effect {
    public static StunEffect instance;
    public StunEffect() {
        super(EffectType.HARMFUL, 0xeeeeee);

        setRegistryName("stun");

        addAttributesModifier(Attributes.MOVEMENT_SPEED, "c2e930ec-9683-4bd7-bc04-8e6ff6587def", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributesModifier(Attributes.ATTACK_DAMAGE, "d59dc254-beb1-4db6-8dfd-c55c0f5554af", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributesModifier(Attributes.ATTACK_KNOCKBACK, "b23dcb72-baf6-4f57-b96a-60d4b629cfd6", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        instance = this;
    }

    @Override
    public void performEffect(LivingEntity entity, int amplifier) {
        Vector3d pos = entity.getEyePosition(0);
        double time = System.currentTimeMillis() / 1000d * Math.PI;
        double xOffset = Math.cos(time) * 0.4;
        double zOffset = Math.sin(time) * 0.4;
        entity.getEntityWorld().addParticle(ParticleTypes.ENTITY_EFFECT, pos.x + xOffset, pos.y + 0.1, pos.z + zOffset, 255, 255, 255);
        entity.getEntityWorld().addParticle(ParticleTypes.ENTITY_EFFECT, pos.x - xOffset, pos.y + 0.4, pos.z - zOffset, 128, 128, 128);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 4 == 0;
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
