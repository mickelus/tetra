package se.mickelus.tetra.blocks.workbench.gui;
import net.minecraftforge.common.ToolType;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiStringOutline;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.gui.GuiTextures;

import java.util.HashMap;
import java.util.Map;

public class GuiTool extends GuiElement {
    public static final int width = 16;
    protected ToolType toolType;

    private GuiString levelIndicator;

    public GuiTool(int x, int y, ToolType toolType) {
        super(x, y, width, 16);
        this.toolType = toolType;

        addChild(new GuiTexture(0, 0, 16, 16, getOffset(toolType) * 16, 52, GuiTextures.workbench));

        levelIndicator = new GuiStringOutline(10, 8, "");
        addChild(levelIndicator);
    }

    public void update(int level, int color) {
        levelIndicator.setVisible(level >= 0);
        levelIndicator.setString(level + "");
        levelIndicator.setColor(color);
    }

    public ToolType getToolType() {
        return toolType;
    }

    protected int getOffset(ToolType tool) {
        if (ToolTypes.hammer.equals(tool)) {
            return 0;
        }
        if (ToolType.AXE.equals(tool)) {
            return 1;
        }
        if (ToolType.PICKAXE.equals(tool)) {
            return 2;
        }
        if (ToolType.SHOVEL.equals(tool)) {
            return 3;
        }
        if (ToolTypes.cut.equals(tool)) {
            return 4;
        }
        if (ToolTypes.pry.equals(tool)) {
            return 5;
        }
        if (ToolType.HOE.equals(tool)) {
            return 6;
        }

        return 14;
    }
}
