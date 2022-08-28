package se.mickelus.tetra.effect.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class EarthboundPotionEffect extends MobEffect {
    public static final String identifier = "earthbound";
    public static EarthboundPotionEffect instance;

    public EarthboundPotionEffect() {
        super(MobEffectCategory.HARMFUL, 0x006600);

        addAttributeModifier(Attributes.MOVEMENT_SPEED, "dc6d6b51-a5da-4735-9277-41fd355829f5", -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, "4134bd78-8b75-46fe-bd9e-cbddff983181", 1, AttributeModifier.Operation.MULTIPLY_TOTAL);

        instance = this;
    }
}
