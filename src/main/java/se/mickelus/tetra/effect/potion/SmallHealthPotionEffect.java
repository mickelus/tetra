package se.mickelus.tetra.effect.potion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SmallHealthPotionEffect extends MobEffect {
    public static final String identifier = "small_health";
    public static SmallHealthPotionEffect instance;

    public SmallHealthPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xbb4444);

        addAttributeModifier(Attributes.MAX_HEALTH, ResourceLocation.fromNamespaceAndPath("tetra", "small_health_max_health"), 1,
                AttributeModifier.Operation.ADD_VALUE);

        instance = this;
    }
}
