package se.mickelus.tetra.gui;

import se.mickelus.tetra.gui.animation.KeyframeAnimation;

import java.util.HashSet;
import java.util.Set;

public class GuiElement extends GuiNode {

    protected int x;
    protected int y;

    protected int width;
    protected int height;

    protected float opacity = 1;

    protected boolean hasFocus = false;

    protected Set<KeyframeAnimation> activeAnimations;

    public GuiElement(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.activeAnimations = new HashSet<>();
    }

    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        activeAnimations.removeIf(keyframeAnimation -> !keyframeAnimation.isActive());
        activeAnimations.forEach(KeyframeAnimation::preDraw);
        super.draw(refX + x, refY + y, screenWidth, screenHeight, mouseX, mouseY, opacity * this.opacity);
        calculateFocusState(refX, refY, mouseX, mouseY);
    }

    @Override
    public boolean onClick(int x, int y) {
        for (GuiNode element : elements) {
            if (element.isVisible()) {
                if (element.onClick(x, y)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected void calculateFocusState(int refX, int refY, int mouseX, int mouseY) {
        boolean gainFocus = mouseX >= x + refX && mouseX < x + refX + width && mouseY >= y + refY && mouseY < y + refY + height;

        if (gainFocus != hasFocus) {
            hasFocus = gainFocus;
            if (hasFocus) {
                onFocus();
            } else {
                onBlur();
            }
        }
    }

    protected void onFocus() {

    }

    protected void onBlur() {

    }

    public boolean hasFocus() {
        return hasFocus;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public GuiElement setOpacity(float opacity) {
        this.opacity = opacity;
        return this;
    }

    public float getOpacity() {
        return opacity;
    }

    public void addAnimation(KeyframeAnimation animation) {
        activeAnimations.add(animation);
    }

    public void removeAnimation(KeyframeAnimation animation) {
        activeAnimations.remove(animation);
    }

    protected static int colorWithOpacity(int color, float opacity) {
        return colorWithOpacity(color, Math.round(opacity * 255));
    }

    protected static int colorWithOpacity(int color, int opacity) {
        return color & 0xffffff | (opacity << 24);
    }
}
