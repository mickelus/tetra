package se.mickelus.tetra.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import se.mickelus.mgui.gui.GuiElement;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class InvertColorGui extends GuiElement {

    public InvertColorGui(int x, int y) {
        super(x, y, 0, 0);
    }

    public InvertColorGui(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected void drawChildren(PoseStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        super.drawChildren(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
        RenderSystem.defaultBlendFunc();
    }
}
