package se.mickelus.tetra.effect.potion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class UnwaveringPotionEffect extends MobEffect {
    public static final String identifier = "unwavering";
    public static UnwaveringPotionEffect instance;

    public UnwaveringPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x887700);

        addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, ResourceLocation.fromNamespaceAndPath("tetra", "unwavering_knockback_resistance"), 1,
                AttributeModifier.Operation.ADD_VALUE);

        instance = this;
    }
}
