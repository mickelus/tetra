package se.mickelus.tetra.effect.potion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SteeledPotionEffect extends MobEffect {
    public static final String identifier = "steeled";
    public static SteeledPotionEffect instance;

    public SteeledPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x880000);

        addAttributeModifier(Attributes.ARMOR, ResourceLocation.fromNamespaceAndPath("tetra", "steeled_armor"), 1,
                AttributeModifier.Operation.ADD_VALUE);

        instance = this;
    }
}
