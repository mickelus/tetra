package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.gui.GuiColors;

import java.util.Collections;
import java.util.List;

public class ToolRequirementGui extends GuiTool {

    int requiredLevel;
    int availableLevel;

    public ToolRequirementGui(int x, int y, ToolType toolType) {
        super(x, y, toolType);
    }

    public ToolRequirementGui updateRequirement(int requiredLevel, int availableLevel) {
        setVisible(requiredLevel != 0);
        this.requiredLevel = requiredLevel;
        this.availableLevel = availableLevel;

        if (isVisible()) {
            if (requiredLevel > availableLevel) {
                update(requiredLevel, GuiColors.remove);
            } else {
                update(requiredLevel, GuiColors.add);
            }
        }

        return this;
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            return Collections.singletonList(I18n.format("tetra.tool." + toolType.getName() + ".requirement", requiredLevel) + "\n \n"
                    + (requiredLevel > availableLevel ? TextFormatting.RED : TextFormatting.GREEN)
                    + I18n.format( "tetra.tool.available", availableLevel));
        }
        return super.getTooltipLines();
    }
}
