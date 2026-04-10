package se.mickelus.tetra.effect.potion;

import net.minecraft.resources.ResourceLocation;
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

        addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath("tetra", "earthbound_movement_speed"), -0.3,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE,
                ResourceLocation.fromNamespaceAndPath("tetra", "earthbound_knockback_resistance"), 1,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        instance = this;
    }
}
