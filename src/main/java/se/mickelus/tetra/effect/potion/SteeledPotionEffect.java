package se.mickelus.tetra.effect.potion;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.util.ParticleHelper;

public class SteeledPotionEffect extends Effect {
    public static SteeledPotionEffect instance;

    public SteeledPotionEffect() {
        super(EffectType.BENEFICIAL, 0x880000);

        setRegistryName("steeled");

        addAttributeModifier(Attributes.ARMOR, "62eba42f-3fe5-436c-812d-2f5ef72bc55f", 1, AttributeModifier.Operation.ADDITION);

        instance = this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack mStack, int x, int y, float z) {
        super.renderInventoryEffect(effect, gui, mStack, x, y, z);

        int amount = effect.getAmplifier() + 1;
        EffectHelper.renderInventoryEffectTooltip(gui, mStack, x, y, () ->
                new StringTextComponent(I18n.get("effect.tetra.steeled.tooltip", amount)));
    }
}
