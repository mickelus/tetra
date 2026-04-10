package se.mickelus.tetra.effect.potion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SmallStrengthPotionEffect extends MobEffect {
    public static final String identifier = "small_strength";
    public static SmallStrengthPotionEffect instance;

    public SmallStrengthPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x880000);

        addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.fromNamespaceAndPath("tetra", "small_strength_attack_damage"), 1,
                AttributeModifier.Operation.ADD_VALUE);

        instance = this;
    }
}
