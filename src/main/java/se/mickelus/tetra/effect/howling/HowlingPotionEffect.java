package se.mickelus.tetra.effect.howling;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class HowlingPotionEffect extends MobEffect {
    public static final String identifier = "howling";
    public static HowlingPotionEffect instance;

    public HowlingPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xeeeeee);

        addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath("tetra", "howling_movement_speed"), -0.05,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        instance = this;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            double offset = Math.PI * 4 / (amplifier + 1);
            for (int i = 0; i < (amplifier + 1) / 2; i++) {
                double time = System.currentTimeMillis() / 1000d * Math.PI + offset * i;
                double xOffset = -Math.cos(time);
                double zOffset = Math.sin(time);
                Vec3 pos = entity.position().add(xOffset, 0.1 + Math.random() * entity.getBbHeight(), zOffset);
                entity.getCommandSenderWorld().addParticle(ParticleTypes.POOF, pos.x, pos.y, pos.z, -Math.cos(time - Math.PI / 2) * 0.1, 0.01, Math.sin(time - Math.PI / 2) * 0.1);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}
