package se.mickelus.tetra.effect.potion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ExhaustedPotionEffect extends MobEffect {
    public static final String identifier = "exhausted";
    public static ExhaustedPotionEffect instance;

    public ExhaustedPotionEffect() {
        super(MobEffectCategory.HARMFUL, 0x222222);

        addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath("tetra", "exhausted_movement_speed"), -0.1,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED, ResourceLocation.fromNamespaceAndPath("tetra", "exhausted_attack_speed"), -0.05,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        instance = this;
    }

    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity().hasEffect(se.mickelus.tetra.effect.EffectHelper.effectHolder(instance))) {
            float multiplier = 1 - (event.getEntity().getEffect(se.mickelus.tetra.effect.EffectHelper.effectHolder(instance)).getAmplifier() + 1) * 0.05f;
            event.setNewSpeed(event.getNewSpeed() * multiplier);
        }
    }
}
