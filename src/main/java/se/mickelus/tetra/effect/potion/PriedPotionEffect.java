package se.mickelus.tetra.effect.potion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import se.mickelus.mutil.util.ParticleHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PriedPotionEffect extends MobEffect {
    public static final String identifier = "pried";
    public static PriedPotionEffect instance;

    public PriedPotionEffect() {
        super(MobEffectCategory.HARMFUL, 0x880000);

        addAttributeModifier(Attributes.ARMOR, ResourceLocation.fromNamespaceAndPath("tetra", "pried_armor"), -1,
                AttributeModifier.Operation.ADD_VALUE);

        instance = this;
    }

    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.getCommandSenderWorld().isClientSide) {
            ParticleHelper.spawnArmorParticles(entity);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}
