package se.mickelus.tetra.effect.potion;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class StunPotionEffect extends MobEffect {
    public static StunPotionEffect instance;
    public StunPotionEffect() {
        super(MobEffectCategory.HARMFUL, 0xeeeeee);

        setRegistryName("stun");

        addAttributeModifier(Attributes.MOVEMENT_SPEED, "c2e930ec-9683-4bd7-bc04-8e6ff6587def", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "d59dc254-beb1-4db6-8dfd-c55c0f5554af", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_KNOCKBACK, "b23dcb72-baf6-4f57-b96a-60d4b629cfd6", -1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        instance = this;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.getCommandSenderWorld().isClientSide) {
            Vec3 pos = entity.getEyePosition(0);
            double time = System.currentTimeMillis() / 1000d * Math.PI;
            double xOffset = Math.cos(time) * 0.4;
            double zOffset = Math.sin(time) * 0.4;

            ((ServerLevel) entity.getCommandSenderWorld()).sendParticles(ParticleTypes.ENTITY_EFFECT, pos.x + xOffset, pos.y + 0.1, pos.z + zOffset,
                    1, 0, 0, 0, 0);
            ((ServerLevel) entity.getCommandSenderWorld()).sendParticles(ParticleTypes.ENTITY_EFFECT, pos.x - xOffset, pos.y + 0.4, pos.z - zOffset,
                    1, 0, 0, 0, 0);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 4 == 0;
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
