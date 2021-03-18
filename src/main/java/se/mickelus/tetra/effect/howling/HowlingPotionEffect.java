package se.mickelus.tetra.effect.howling;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.TetraMod;

public class HowlingPotionEffect extends Effect {
    public static HowlingPotionEffect instance;

    public HowlingPotionEffect() {
        super(EffectType.BENEFICIAL, 0xeeeeee);

        setRegistryName("howling");

        addAttributesModifier(Attributes.MOVEMENT_SPEED, "f80b9432-480d-4846-b9f9-178157dbac07", -0.05, AttributeModifier.Operation.MULTIPLY_BASE);
        instance = this;
    }

    @Override
    public void performEffect(LivingEntity entity, int amplifier) {
        if (entity.world.isRemote) {
            double offset = Math.PI * 4 / (amplifier + 1);
            for (int i = 0; i < (amplifier + 1) / 2; i++) {
                double time = System.currentTimeMillis() / 1000d * Math.PI + offset * i;
                double xOffset = -Math.cos(time);
                double zOffset = Math.sin(time);
                Vector3d pos = entity.getPositionVec().add(xOffset, 0.1 + Math.random() * entity.getHeight(), zOffset);
                entity.getEntityWorld().addParticle(ParticleTypes.POOF, pos.x, pos.y, pos.z, -Math.cos(time - Math.PI / 2) * 0.1, 0.01, Math.sin(time - Math.PI / 2) * 0.1);
            }
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}
