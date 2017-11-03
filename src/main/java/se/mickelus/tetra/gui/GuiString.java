package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class GuiString extends GuiElement {

    private String string;

    private FontRenderer fontRenderer;

    private int color = 0xffffffff;

    private GuiAlignment textAlign = GuiAlignment.left;

    public GuiString(int x, int y, String string) {
        super(x, y, 0 ,0);

        this.string = string;

        fontRenderer = Minecraft.getMinecraft().fontRendererObj;
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
        this.string = string;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        if (textAlign == GuiAlignment.left) {
            drawString(fontRenderer, string, refX + x, refY + y, color);
        } else if (textAlign == GuiAlignment.center) {
            drawCenteredString(fontRenderer, string, refX + x, refY + y, color);
        } else if (textAlign == GuiAlignment.right) {
            drawString(fontRenderer, string, refX + x - fontRenderer.getStringWidth(string), refY + y, color);
        }
    }
}
