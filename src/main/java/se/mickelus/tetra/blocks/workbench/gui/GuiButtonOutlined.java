package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.Minecraft;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.gui.impl.GuiColors;

public class GuiButtonOutlined extends GuiClickable {
    private static final String WORKBENCH_TEXTURE = "textures/gui/workbench.png";

    private GuiTexture borderLeft;
    private GuiTexture borderRight;
    private GuiRect borderTop;
    private GuiRect borderBottom;

    public GuiButtonOutlined(int x, int y, String label, Runnable onClickHandler) {
        this(x, y, label, GuiAlignment.left, onClickHandler);
    }

    public GuiButtonOutlined(int x, int y, String label, GuiAlignment alignment, Runnable onClickHandler) {
        super(x, y, 0, 11, onClickHandler);
        width = Minecraft.getMinecraft().fontRenderer.getStringWidth(label) + 18;

        addChild(new GuiRect(9, 0, width - 18, 11, 0));

        borderLeft = new GuiTexture(0, 0, 9, 11, 79, 0, WORKBENCH_TEXTURE).setColor(GuiColors.muted);
        addChild(borderLeft);
        borderRight = new GuiTexture(width - 9, 0, 9, 11, 88, 0, WORKBENCH_TEXTURE).setColor(GuiColors.muted);
        addChild(borderRight);

        borderTop = new GuiRect(9, 1, width - 18, 1, GuiColors.muted);
        addChild(borderTop);
        borderBottom = new GuiRect(9, 9, width - 18, 1, GuiColors.muted);
        addChild(borderBottom);

        addChild(new GuiStringOutline(9, 1, label));
    }

    private void setBorderColors(int color) {
        borderLeft.setColor(color);
        borderRight.setColor(color);
        borderTop.setColor(color);
        borderBottom.setColor(color);
    }

    @Override
    protected void onFocus() {
        setBorderColors(GuiColors.hoverMuted);
    }

    @Override
    protected void onBlur() {
        setBorderColors(GuiColors.muted);
    }
}
