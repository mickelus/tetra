package se.mickelus.tetra.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class GuiButton extends GuiClickable {

    private static final int COLOR_DEFAULT = 0xffffffff;
    private static final int COLOR_HOVER = 0xffffff00;
    private static final int COLOR_DISABLED = 0x66ffffff;

    private final String text;

    private final FontRenderer fontRenderer;

    private boolean enabled = true;
    private String disabledText;



    public GuiButton(int x, int y, int width, int height, String text, Runnable onClick) {
        super(x, y, width, height, onClick);

        this.text = text;

        fontRenderer = Minecraft.getMinecraft().fontRenderer;
    }

    public GuiButton(int x, int y, int width, int height, String text, Runnable onClick, String disabledText) {
        this(x, y, width, height, text, onClick);

        this.disabledText = disabledText;
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        GlStateManager.enableBlend();
        if (!enabled) {
            fontRenderer.drawString(text, refX + x, refY + y, COLOR_DISABLED);
        } else if (hasFocus()) {
            fontRenderer.drawString(text, refX + x, refY + y, COLOR_HOVER);
        } else {
            fontRenderer.drawString(text, refX + x, refY + y, COLOR_DEFAULT);
        }
        GlStateManager.disableBlend();
    }

    @Override
    public boolean onClick(int x, int y) {
        return enabled && super.onClick(x, y);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
