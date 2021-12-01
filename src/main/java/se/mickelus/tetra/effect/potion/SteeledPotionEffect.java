package se.mickelus.tetra.effect.potion;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.effect.EffectHelper;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class SteeledPotionEffect extends MobEffect {
    public static SteeledPotionEffect instance;

    public SteeledPotionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x880000);

        setRegistryName("steeled");

        addAttributeModifier(Attributes.ARMOR, "62eba42f-3fe5-436c-812d-2f5ef72bc55f", 1, AttributeModifier.Operation.ADDITION);

        instance = this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack mStack, int x, int y, float z) {
        super.renderInventoryEffect(effect, gui, mStack, x, y, z);

        int amount = effect.getAmplifier() + 1;
        EffectHelper.renderInventoryEffectTooltip(gui, mStack, x, y, () ->
                new TextComponent(I18n.get("effect.tetra.steeled.tooltip", amount)));
    }
}
