package se.mickelus.tetra.gui;

import net.minecraft.client.renderer.GlStateManager;

public class GuiRect extends GuiElement {

    private int color;
    private boolean offset;

    public GuiRect(int x, int y, int width, int height, int color) {
        this(x, y, width, height, color, false);
    }

    public GuiRect(int x, int y, int width, int height, int color, boolean offset) {
        super(x, y, offset ? width + 1 : width, offset ? height + 1 : height);

        this.color = color;
        this.offset = offset;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        if (offset) {
            GlStateManager.translate(0.5F, 0.5F, 0);
            drawRect(refX + x, refY + y, refX + x + width - 1, refY + y + height - 1, color);
            GlStateManager.translate(-0.5F, -0.5F, 0);
        } else {
            drawRect(refX + x, refY + y, refX + x + width, refY + y + height, color);
        }
        GlStateManager.color(255, 255, 255, 255);
    }
}
