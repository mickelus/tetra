package se.mickelus.tetra.blocks.workbench.gui;

import se.mickelus.mutil.gui.GuiTextureOffset;
import se.mickelus.tetra.gui.GuiTextures;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class GuiModuleBackdrop extends GuiTextureOffset {

    public GuiModuleBackdrop(int x, int y, int color) {
        this(x, y, color, 15, 15, 52, 0);
    }

    protected GuiModuleBackdrop(int x, int y, int color, int width, int height, int textureX, int textureY) {
        super(x, y, width, height, textureX, textureY, GuiTextures.workbench);

        this.color = color;
    }
}
