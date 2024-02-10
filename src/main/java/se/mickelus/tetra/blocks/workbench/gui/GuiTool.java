package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.mutil.gui.GuiStringOutline;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.TetraToolActions;
import se.mickelus.tetra.client.ToolActionIconStore;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.module.data.GlyphData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class GuiTool extends GuiElement {
    public static final int width = 16;
    private final GuiString levelIndicator;

    protected GuiElement iconContainer;
    protected ToolAction toolAction;

    private GlyphData fallback = new GlyphData(GuiTextures.toolActions, 240, 0);

    public GuiTool(int x, int y, ToolAction toolAction) {
        super(x, y, width, 16);
        this.toolAction = toolAction;

        iconContainer = new GuiElement(0, 0, 16, 16);
        addChild(iconContainer);

        updateIcon();

        levelIndicator = new GuiStringOutline(10, 8, "");
        addChild(levelIndicator);
    }

    public void update(int level, int color) {
        levelIndicator.setVisible(level >= 0);
        levelIndicator.setString(level + "");
        levelIndicator.setColor(color);

        updateIcon();
    }

    protected void updateIcon() {
        iconContainer.clearChildren();
        GlyphData glyph = Optional.ofNullable(ToolActionIconStore.instance.getIcon(toolAction)).orElse(fallback);
        iconContainer.addChild(new GuiTexture(0, 0, 16, 16, glyph.textureX, glyph.textureY, glyph.textureLocation));
    }

    public ToolAction getToolAction() {
        return toolAction;
    }

    protected int getOffset(ToolAction tool) {
        if (TetraToolActions.hammer.equals(tool)) {
            return 0;
        }
        if (ToolActions.AXE_DIG.equals(tool)) {
            return 1;
        }
        if (ToolActions.PICKAXE_DIG.equals(tool)) {
            return 2;
        }
        if (ToolActions.SHOVEL_DIG.equals(tool)) {
            return 3;
        }
        if (TetraToolActions.cut.equals(tool)) {
            return 4;
        }
        if (TetraToolActions.pry.equals(tool)) {
            return 5;
        }
        if (ToolActions.HOE_DIG.equals(tool)) {
            return 6;
        }

        return 14;
    }
}
