package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;

public class GuiString extends GuiElement {

    protected String string;

    protected FontRenderer fontRenderer;

    protected int color = 0xffffffff;
    protected boolean drawShadow = true;

    protected boolean fixedWidth = false;

    public GuiString(int x, int y, String string) {
        super(x, y, 0, 9);

        fontRenderer = Minecraft.getMinecraft().fontRenderer;

        this.string = string;
        width = fontRenderer.getStringWidth(string);
    }

    public GuiString(int x, int y, int width, String string) {
        super(x, y, width, 9);

        fixedWidth = true;

        fontRenderer = Minecraft.getMinecraft().fontRenderer;

        this.string = fontRenderer.trimStringToWidth(string, width);
    }

    public GuiString(int x, int y, String string, GuiAttachment attachment) {
        this(x, y, string);

        attachmentPoint = attachment;
    }

    public GuiString(int x, int y, String string, int color) {
        this(x, y, string);

        this.color = color;
    }

    public GuiString(int x, int y, String string, int color, GuiAttachment attachment) {
        this(x, y, string, attachment);

        this.color = color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setString(String string) {
        if (string != null & !string.equals(this.string)) {
            if (fixedWidth) {
                this.string = fontRenderer.trimStringToWidth(string, width);
            } else {
                this.string = string;
                width = fontRenderer.getStringWidth(string);
            }
        }
    }

    public GuiString setShadow(boolean shadow) {
        drawShadow = shadow;
        return this;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        activeAnimations.removeIf(keyframeAnimation -> !keyframeAnimation.isActive());
        activeAnimations.forEach(KeyframeAnimation::preDraw);
        GlStateManager.enableBlend();
        drawString(string, refX + x, refY + y, color, opacity * getOpacity(), drawShadow);
    }

    protected void drawString(String text, int x, int y, int color, float opacity, boolean drawShadow) {
        color = colorWithOpacity(color, opacity);

        // if the vanilla fontrender considers the color to be almost transparent (0xfc) it flips the opacity back to 1
        if ((color & -67108864) != 0) {
            fontRenderer.drawString(text, (float)x, (float)y, color, drawShadow);
        }
    }
}
