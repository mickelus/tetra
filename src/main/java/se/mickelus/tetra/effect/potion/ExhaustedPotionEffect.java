package se.mickelus.tetra.effect.potion;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.EffectRenderer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import se.mickelus.tetra.effect.gui.EffectTooltipRenderer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class ExhaustedPotionEffect extends MobEffect {
    public static final String identifier = "exhausted";
    public static ExhaustedPotionEffect instance;

    public ExhaustedPotionEffect() {
        super(MobEffectCategory.HARMFUL, 0x222222);

        addAttributeModifier(Attributes.MOVEMENT_SPEED, "19be7b9d-fff9-4ccf-a811-0a378da5a269", -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED, "05b3352c-4c10-4c52-92ce-9dc8a679e7f0", -0.05, AttributeModifier.Operation.MULTIPLY_TOTAL);

        instance = this;
    }

    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntityLiving().hasEffect(instance)) {
            float multiplier = 1 - (event.getEntityLiving().getEffect(instance).getAmplifier() + 1) * 0.05f;
            event.setNewSpeed(event.getNewSpeed() * multiplier);
        }
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<EffectRenderer> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new EffectTooltipRenderer(effect -> {
            int amount = effect.getAmplifier() + 1;
            return I18n.get("effect.tetra.exhausted.tooltip", amount * 10, amount * 5);
        }));
    }
}
