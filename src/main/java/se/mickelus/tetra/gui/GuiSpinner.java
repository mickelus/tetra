package se.mickelus.tetra.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import se.mickelus.mgui.gui.GuiElement;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class GuiSpinner extends GuiElement {
    public GuiSpinner(int x, int y) {
        super(x, y, 5, 5);
    }

    @Override
    public void draw(PoseStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        drawBlip(matrixStack, refX + x, refY + y, 0);
        drawBlip(matrixStack, refX + x + 2, refY + y - 1, 1);
        drawBlip(matrixStack, refX + x + 4, refY + y, 2);
        drawBlip(matrixStack, refX + x + 5, refY + y + 2, 3);
        drawBlip(matrixStack, refX + x + 4, refY + y + 4, 4);
        drawBlip(matrixStack, refX + x + 2, refY + y + 5, 5);
        drawBlip(matrixStack, refX + x, refY + y + 4, 6);
        drawBlip(matrixStack, refX + x - 1, refY + y + 2, 7);
    }

    private void drawBlip(PoseStack matrixStack, int x, int y, int offset) {
        float opacity = 1 - Math.max((System.currentTimeMillis() - offset * 200) % 1600 / 1600f, 0f);
        drawRect(matrixStack, x, y, x + 1, y + 1, GuiColors.normal, opacity);
    }
}
