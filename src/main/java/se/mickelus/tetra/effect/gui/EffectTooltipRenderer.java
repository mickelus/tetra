package se.mickelus.tetra.effect.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.EffectRenderer;
import se.mickelus.tetra.effect.EffectHelper;

import java.util.function.Function;

public class EffectTooltipRenderer extends EffectRenderer {
    private final Function<MobEffectInstance, String> constructEffectTooltip;

    public EffectTooltipRenderer(Function<MobEffectInstance, String> constructEffectTooltip) {
        this.constructEffectTooltip = constructEffectTooltip;
    }

    @Override
    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack mStack, int x, int y, float z) {
        EffectHelper.renderInventoryEffectTooltip(gui, mStack, x, y, () -> new TextComponent(constructEffectTooltip.apply(effect)));
    }

    @Override
    public void renderHUDEffect(MobEffectInstance effect, GuiComponent gui, PoseStack mStack, int x, int y, float z, float alpha) {
    }
}
