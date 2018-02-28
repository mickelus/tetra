package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class GuiString extends GuiElement {

    protected String string;

    protected FontRenderer fontRenderer;

    protected int color = 0xffffffff;
    protected boolean drawShadow = true;

    protected GuiAlignment textAlign = GuiAlignment.left;

    public GuiString(int x, int y, String string) {
        super(x, y, 0 ,0);

        this.string = string;

        fontRenderer = Minecraft.getMinecraft().fontRenderer;
    }

    public GuiString(int x, int y, int width, String string) {
        super(x, y, width ,0);


        fontRenderer = Minecraft.getMinecraft().fontRenderer;
        this.string = fontRenderer.trimStringToWidth(string, width);
    }

    public GuiString(int x, int y, String string, GuiAlignment textAlign) {
        this(x, y, string);

        this.textAlign = textAlign;
    }

    public GuiString(int x, int y, String string, int color) {
        this(x, y, string);

        this.color = color;
    }

    public GuiString(int x, int y, String string, int color, GuiAlignment textAlign) {
        this(x, y, string);

        this.color = color;
        this.textAlign = textAlign;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setString(String string) {
        if (width > 0) {
            this.string = fontRenderer.trimStringToWidth(string, width);
        } else {
            this.string = string;
        }
    }

    public GuiString setShadow(boolean shadow) {
        drawShadow = shadow;
        return this;
    }

    public void setTextAlignment(GuiAlignment textAlign) {
        this.textAlign = textAlign;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        if (textAlign == GuiAlignment.left) {
            drawString(string, refX + x, refY + y, color, drawShadow);
        } else if (textAlign == GuiAlignment.center) {
            drawCenteredString(string, refX + x, refY + y, color, drawShadow);
        } else if (textAlign == GuiAlignment.right) {
            drawString(string, refX + x - fontRenderer.getStringWidth(string), refY + y, color, drawShadow);
        }
    }

    protected void drawString(String text, int x, int y, int color, boolean drawShadow) {
        fontRenderer.drawString(text, (float)x, (float)y, color, drawShadow);
    }

    protected void drawCenteredString(String text, int x, int y, int color, boolean drawShadow) {
        drawString(text, (x - fontRenderer.getStringWidth(text) / 2), y, color, drawShadow);
    }
}
