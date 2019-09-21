package se.mickelus.tetra.gui;

import net.minecraft.client.renderer.GlStateManager;

public class GuiTextSmall extends GuiText {

    public GuiTextSmall(int x, int y, int width, String string) {
        super(x, y, width , string);
    }

    public void setString(String string) {
        this.string = string.replace("\\n", "\n");

        height = fontRenderer.getWordWrappedHeight(this.string, width * 2) / 2;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(.5, .5, .5);
        fontRenderer.drawSplitString(string, (refX + x) * 2, (refY + y) * 2, width*2, 0xffffffff);
        GlStateManager.popMatrix();
    }
}
