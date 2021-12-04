package se.mickelus.tetra.effect.potion;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.EffectRenderer;
import se.mickelus.tetra.effect.gui.EffectTooltipRenderer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class SmallAbsorbPotionEffect extends MobEffect {
    public static SmallAbsorbPotionEffect instance;

    public SmallAbsorbPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 2445989);

        setRegistryName("small_absorb");

        instance = this;
    }

    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeManager, int amplifier) {
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() - (amplifier + 1));
        super.removeAttributeModifiers(entity, attributeManager, amplifier);
    }

    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeManager, int amplifier) {
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() + amplifier + 1);
        super.addAttributeModifiers(entity, attributeManager, amplifier);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<EffectRenderer> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new EffectTooltipRenderer(effect -> I18n.get("effect.tetra.small_absorb.tooltip", effect.getAmplifier() + 1)));
    }
}
