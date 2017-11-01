package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.renderer.GlStateManager;
import se.mickelus.tetra.gui.GuiTextureOffset;


public class GuiModuleBackdrop extends GuiTextureOffset {
    public static final int COLOR_NORMAL = 0xffffff;
    public static final int COLOR_ADD = 0xaaffaa;
    public static final int COLOR_REMOVE = 0xffaaaa;
    public static final int COLOR_CHANGE = 0xaaaaff;

    private int color;

    public GuiModuleBackdrop(int x, int y, int color) {
        super(x, y, 15, 15, 52, 0, "textures/gui/workbench.png");
        this.color = color;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        float red = (float)(color >> 16 & 255) / 255.0F;
        float blue = (float)(color >> 8 & 255) / 255.0F;
        float green = (float)(color & 255) / 255.0F;
        GlStateManager.color(red, blue, green);
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY);
        GlStateManager.color(1, 1, 1, 1);
    }
}
