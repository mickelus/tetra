package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.gui.GuiColors;

import java.util.Collections;
import java.util.List;

public class ToolRequirementGui extends GuiTool {

    private int requiredLevel;
    private int availableLevel;

    private boolean showTooltip = true;
    private boolean showTooltipRequirement = true;

    String requirementTooltip;

    public ToolRequirementGui(int x, int y, ToolType toolType) {
        this(x, y, toolType, "tetra.tool." + toolType.getName() + ".requirement");
    }
    public ToolRequirementGui(int x, int y, ToolType toolType, String requirementTooltip) {
        super(x, y, toolType);

        this.requirementTooltip = requirementTooltip;
    }

    public ToolRequirementGui setTooltipVisibility(boolean shouldShow) {
        showTooltip = shouldShow;
        return this;
    }

    public ToolRequirementGui setTooltipRequirementVisibility(boolean shouldShow) {
        showTooltipRequirement = shouldShow;
        return this;
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
        if (hasFocus() && showTooltip) {
            if (showTooltipRequirement) {
                return Collections.singletonList(I18n.get(requirementTooltip, requiredLevel) + "\n \n"
                        + (requiredLevel > availableLevel ? TextFormatting.RED : TextFormatting.GREEN)
                        + I18n.get( "tetra.tool.available", availableLevel));

            }

            return Collections.singletonList(I18n.get(requirementTooltip, requiredLevel));
        }
        return super.getTooltipLines();
    }
}
