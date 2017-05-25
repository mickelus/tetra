package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class GuiButton extends GuiElement {

    private static final int COLOR_DEFAULT = 0xffffffff;
    private static final int COLOR_HOVER = 0xffffff00;

    private final Runnable onClickHandler;

    private final String text;

    private final FontRenderer fontRenderer;

    private int screenX;
    private int screenY;

    public GuiButton(int x, int y, int width, int height, String text, Runnable onClick) {
        super(x, y, width, height);

        this.text = text;

        onClickHandler = onClick;

        fontRenderer = Minecraft.getMinecraft().fontRendererObj;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY);
        screenX = refX + x;
        screenY = refY + y;

        fontRenderer.drawString(text, x + refX, y + refY, hasFocus() ? COLOR_HOVER : COLOR_DEFAULT);
    }

    @Override
    public void onClick(int x, int y) {
        if (x >= screenX && x <= x + screenX + width && y >= screenY && y <= screenY + height) {
            onClickHandler.run();
        }
    }
}
