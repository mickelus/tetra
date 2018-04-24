package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

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
        if (fixedWidth) {
            this.string = fontRenderer.trimStringToWidth(string, width);
        } else {
            this.string = string;
            width = fontRenderer.getStringWidth(string);
        }
    }

    public GuiString setShadow(boolean shadow) {
        drawShadow = shadow;
        return this;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        drawString(string, refX + x, refY + y, color, drawShadow);
    }

    protected void drawString(String text, int x, int y, int color, boolean drawShadow) {
        fontRenderer.drawString(text, (float)x, (float)y, color, drawShadow);
    }
}
