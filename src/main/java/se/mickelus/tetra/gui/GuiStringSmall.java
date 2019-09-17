package se.mickelus.tetra.gui;

import net.minecraft.client.renderer.GlStateManager;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;

public class GuiStringSmall extends GuiString {

    public GuiStringSmall(int x, int y, String string) {
        super(x*2, y*2, string);
    }

    public GuiStringSmall(int x, int y, String string, int color) {
        super(x*2, y*2, string, color);
    }

    public GuiStringSmall(int x, int y, String string, GuiAttachment attachment) {
        super(x*2, y*2, string, attachment);
    }

    public GuiStringSmall(int x, int y, String string, int color, GuiAttachment attachment) {
        super(x*2, y*2, string, color, attachment);
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
    public int getWidth() {
        return width / 2;
    }

    @Override
    public int getHeight() {
        return height / 2;
    }

    @Override
    protected void calculateFocusState(int refX, int refY, int mouseX, int mouseY) {
        boolean gainFocus = mouseX >= getX() + refX
                && mouseX < getX() + refX + getWidth()
                && mouseY >= getY() + refY
                && mouseY < getY() + refY + getHeight();

        if (gainFocus != hasFocus) {
            hasFocus = gainFocus;
            if (hasFocus) {
                onFocus();
            } else {
                onBlur();
            }
        }
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        calculateFocusState(refX, refY, mouseX, mouseY);
        activeAnimations.removeIf(keyframeAnimation -> !keyframeAnimation.isActive());
        activeAnimations.forEach(KeyframeAnimation::preDraw);
        GlStateManager.pushMatrix();
        GlStateManager.scale(.5, .5, .5);
        GlStateManager.enableBlend();
        drawString(string, refX * 2 + x, refY * 2 + y,
                color, opacity * getOpacity(), drawShadow);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
