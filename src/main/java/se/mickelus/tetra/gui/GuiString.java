package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class GuiString extends GuiElement {

    private String string;

    private FontRenderer fontRenderer;

    public GuiString(int x, int y, String string) {
        super(x, y, 0 ,0);

        this.string = string;

        fontRenderer = Minecraft.getMinecraft().fontRendererObj;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        drawString(fontRenderer, string, refX + x, refY + y, 0xffffffff);
    }
}
