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
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        activeAnimations.removeIf(keyframeAnimation -> !keyframeAnimation.isActive());
        activeAnimations.forEach(KeyframeAnimation::preDraw);
        GlStateManager.pushMatrix();
        GlStateManager.scale(.5, .5, .5);
        GlStateManager.enableBlend();
        drawString(string, refX * 2 + x + getXOffset(this, attachmentPoint), refY * 2 + y + getYOffset(this, attachmentPoint),
                color, opacity * getOpacity(), drawShadow);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
