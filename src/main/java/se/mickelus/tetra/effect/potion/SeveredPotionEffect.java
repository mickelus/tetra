package se.mickelus.tetra.effect.potion;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SeveredPotionEffect extends MobEffect {
    public static final String identifier = "severed";
    public static SeveredPotionEffect instance;

    public SeveredPotionEffect() {
        super(MobEffectCategory.HARMFUL, 0x880000);

        addAttributeModifier(Attributes.MAX_HEALTH, ResourceLocation.fromNamespaceAndPath("tetra", "severed_max_health"), -0.1,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.fromNamespaceAndPath("tetra", "severed_attack_damage"), -0.05,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        instance = this;
    }

    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.getCommandSenderWorld().isClientSide) {
            RandomSource rand = entity.getRandom();
            ((ServerLevel) entity.level()).sendParticles(new DustParticleOptions(new Vector3f(0.5f, 0, 0), 0.5f),
                    entity.getX() + entity.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                    entity.getY() + entity.getBbHeight() * (0.2 + rand.nextGaussian() * 0.4),
                    entity.getZ() + entity.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                    20,
                    0, 0, 0, 0f);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}
