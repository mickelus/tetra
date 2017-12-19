package se.mickelus.tetra.gui;

import net.minecraft.client.renderer.GlStateManager;

public class GuiStringSmall extends GuiString {

    public GuiStringSmall(int x, int y, String string) {
        super(x*2, y*2, string);
    }

    public GuiStringSmall(int x, int y, String string, int color) {
        super(x*2, y*2, string, color);
    }

    public GuiStringSmall(int x, int y, String string, GuiAlignment alignment) {
        super(x*2, y*2, string, alignment);
    }

    public GuiStringSmall(int x, int y, String string, int color, GuiAlignment alignment) {
        super(x*2, y*2, string, color, alignment);
    }

    @Override
    public void setX(int x) {
        super.setX(x * 2);
    }

    @Override
    public void setY(int y) {
        super.setY(y * 2);
    }

    @Override
    public int getX() {
        return super.getX() / 2;
    }

    @Override
    public int getY() {
        return super.getY() / 2;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(.5, .5, .5);
        super.draw(refX*2, refY*2, screenWidth*2, screenHeight*2, mouseX*2, mouseY*2);
        GlStateManager.popMatrix();
    }
}
