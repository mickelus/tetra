package se.mickelus.tetra.effect.potion;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StunPotionEffect extends MobEffect {
    public static final String identifier = "stun";
    public static StunPotionEffect instance;

    public StunPotionEffect() {
        super(MobEffectCategory.HARMFUL, 0xeeeeee);

        addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath("tetra", "stun_movement_speed"), -1,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.fromNamespaceAndPath("tetra", "stun_attack_damage"), -1,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_KNOCKBACK, ResourceLocation.fromNamespaceAndPath("tetra", "stun_attack_knockback"), -1,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        instance = this;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.getCommandSenderWorld().isClientSide) {
            Vec3 pos = entity.getEyePosition(0);
            double time = System.currentTimeMillis() / 1000d * Math.PI;
            double xOffset = Math.cos(time) * 0.4;
            double zOffset = Math.sin(time) * 0.4;
            var particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xEEEEEE);
            ((ServerLevel) entity.getCommandSenderWorld()).sendParticles(particle, pos.x + xOffset, pos.y + 0.1, pos.z + zOffset, 1, 0, 0, 0, 0);
            ((ServerLevel) entity.getCommandSenderWorld()).sendParticles(particle, pos.x - xOffset, pos.y + 0.4, pos.z - zOffset, 1, 0, 0, 0, 0);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 4 == 0;
    }
}
