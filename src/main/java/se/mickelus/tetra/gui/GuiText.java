package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class GuiText extends GuiElement {

    FontRenderer fontRenderer;

    String string;

    public GuiText(int x, int y, int width, String string) {
        super(x, y, width ,0);

        fontRenderer = Minecraft.getMinecraft().fontRenderer;
        setString(string);
    }

    public void setString(String string) {
        this.string = string.replace("\\n", "\n");

        height = fontRenderer.getWordWrappedHeight(this.string, width);
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        fontRenderer.drawSplitString(string, refX + x, refY + y, width, 0xffffffff);
    }
}
