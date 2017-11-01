package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class GuiButton extends GuiElement {

    private static final int COLOR_DEFAULT = 0xffffffff;
    private static final int COLOR_HOVER = 0xffffff00;
    private static final int COLOR_DISABLED = 0x66ffffff;

    private final Runnable onClickHandler;

    private final String text;

    private final FontRenderer fontRenderer;

    private boolean enabled = true;
    private String disabledText;

    /* buttons position on the actual screen, updated on draw to be used when calculating if click events hit this button */
    private int screenX;
    private int screenY;

    public GuiButton(int x, int y, int width, int height, String text, Runnable onClick) {
        super(x, y, width, height);

        this.text = text;

        onClickHandler = onClick;

        fontRenderer = Minecraft.getMinecraft().fontRendererObj;
    }

    public GuiButton(int x, int y, int width, int height, String text, Runnable onClick, String disabledText) {
        this(x, y, width, height, text, onClick);

        this.disabledText = disabledText;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY);
        screenX = refX + x;
        screenY = refY + y;

        GlStateManager.enableBlend();
        if (!enabled) {
            fontRenderer.drawString(text, screenX, screenY, COLOR_DISABLED);
        } else if (hasFocus()) {
            fontRenderer.drawString(text, screenX, screenY, COLOR_HOVER);
        } else {
            fontRenderer.drawString(text, screenX, screenY, COLOR_DEFAULT);
        }
        GlStateManager.disableBlend();
    }

    @Override
    public boolean onClick(int x, int y) {
        if (x >= screenX && x <= screenX + width && y >= screenY && y <= screenY + height && enabled) {
            onClickHandler.run();
            return true;
        }

        return false;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
