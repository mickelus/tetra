package se.mickelus.tetra.blocks.workbench.gui;

import se.mickelus.tetra.gui.GuiTextureOffset;

public class GuiModuleBackdrop extends GuiTextureOffset {
    public static final int COLOR_NORMAL = 0xffffff;
    public static final int COLOR_ADD = 0xaaffaa;
    public static final int COLOR_REMOVE = 0xffaaaa;
    public static final int COLOR_CHANGE = 0xaaaaff;

    public GuiModuleBackdrop(int x, int y, int color) {
        this(x, y, color, 15, 15, 52, 0);
    }

    protected GuiModuleBackdrop(int x, int y, int color, int width, int height, int textureX, int textureY) {
        super(x, y, width, height, textureX, textureY, "textures/gui/workbench.png");

        this.color = color;
    }
}
