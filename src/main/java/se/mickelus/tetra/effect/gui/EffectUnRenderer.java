package se.mickelus.tetra.effect.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.EffectRenderer;

@OnlyIn(Dist.CLIENT)
public class EffectUnRenderer extends EffectRenderer {
    public static final EffectRenderer INSTANCE = new EffectUnRenderer();

    @Override
    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack mStack, int x, int y, float z) {

    }

    @Override
    public void renderHUDEffect(MobEffectInstance effect, GuiComponent gui, PoseStack mStack, int x, int y, float z, float alpha) {

    }

    @Override
    public boolean shouldRender(MobEffectInstance effect) {
        return false;
    }

    @Override
    public boolean shouldRenderHUD(MobEffectInstance effect) {
        return false;
    }
}
