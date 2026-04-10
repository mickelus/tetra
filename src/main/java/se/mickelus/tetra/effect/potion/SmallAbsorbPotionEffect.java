package se.mickelus.tetra.effect.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SmallAbsorbPotionEffect extends MobEffect {
    public static final String identifier = "small_absorb";
    public static SmallAbsorbPotionEffect instance;

    public SmallAbsorbPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 2445989);
        addAttributeModifier(Attributes.MAX_ABSORPTION, ResourceLocation.fromNamespaceAndPath("tetra", "small_absorb"), 1,
                AttributeModifier.Operation.ADD_VALUE);

        instance = this;
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        super.onEffectStarted(entity, amplifier);
        entity.setAbsorptionAmount(Math.max(entity.getAbsorptionAmount(), entity.getMaxAbsorption()));
    }
}
