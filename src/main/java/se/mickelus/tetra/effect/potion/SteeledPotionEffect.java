package se.mickelus.tetra.effect.potion;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.EffectRenderer;
import se.mickelus.tetra.effect.gui.EffectTooltipRenderer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class SteeledPotionEffect extends MobEffect {
    public static final String identifier = "steeled";
    public static SteeledPotionEffect instance;

    public SteeledPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x880000);

        addAttributeModifier(Attributes.ARMOR, "62eba42f-3fe5-436c-812d-2f5ef72bc55f", 1, AttributeModifier.Operation.ADDITION);

        instance = this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<EffectRenderer> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new EffectTooltipRenderer(effect -> I18n.get("effect.tetra.steeled.tooltip", effect.getAmplifier() + 1)));
    }
}
