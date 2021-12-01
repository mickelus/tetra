package se.mickelus.tetra.effect.potion;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.effect.EffectHelper;

public class SmallAbsorbPotionEffect extends Effect {
    public static SmallAbsorbPotionEffect instance;

    public SmallAbsorbPotionEffect() {
        super(EffectType.BENEFICIAL, 2445989);

        setRegistryName("small_absorb");

        instance = this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack mStack, int x, int y, float z) {
        super.renderInventoryEffect(effect, gui, mStack, x, y, z);

        int amount = effect.getAmplifier() + 1;
        EffectHelper.renderInventoryEffectTooltip(gui, mStack, x, y, () ->
                new StringTextComponent(I18n.get("effect.tetra.small_absorb.tooltip", amount)));
    }

    public void removeAttributeModifiers(LivingEntity entity, AttributeModifierManager attributeManager, int amplifier) {
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() - (amplifier + 1));
        super.removeAttributeModifiers(entity, attributeManager, amplifier);
    }

    public void addAttributeModifiers(LivingEntity entity, AttributeModifierManager attributeManager, int amplifier) {
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() + amplifier + 1);
        super.addAttributeModifiers(entity, attributeManager, amplifier);
    }
}
