package se.mickelus.tetra.effect.potion;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.effect.EffectHelper;

public class SmallAbsorbPotionEffect extends MobEffect {
    public static SmallAbsorbPotionEffect instance;

    public SmallAbsorbPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 2445989);

        setRegistryName("small_absorb");

        instance = this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack mStack, int x, int y, float z) {
        super.renderInventoryEffect(effect, gui, mStack, x, y, z);

        int amount = effect.getAmplifier() + 1;
        EffectHelper.renderInventoryEffectTooltip(gui, mStack, x, y, () ->
                new TextComponent(I18n.get("effect.tetra.small_absorb.tooltip", amount)));
    }

    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeManager, int amplifier) {
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() - (amplifier + 1));
        super.removeAttributeModifiers(entity, attributeManager, amplifier);
    }

    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeManager, int amplifier) {
        entity.setAbsorptionAmount(entity.getAbsorptionAmount() + amplifier + 1);
        super.addAttributeModifiers(entity, attributeManager, amplifier);
    }
}
