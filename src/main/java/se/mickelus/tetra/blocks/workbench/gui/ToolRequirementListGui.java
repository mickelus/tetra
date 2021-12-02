package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.Map;
@ParametersAreNonnullByDefault
public class ToolRequirementListGui extends GuiElement {

    private Map<ToolAction, Integer> requiredTools = Collections.emptyMap();

    public ToolRequirementListGui(int x, int y) {
        super(x, y, 54, 18);
    }

    public void update(UpgradeSchematic schematic, ItemStack targetStack, String slot, ItemStack[] materials, Map<ToolAction, Integer> availableTools) {
        boolean hasValidMaterials = schematic.isMaterialsValid(targetStack, slot, materials);
        setVisible(hasValidMaterials);

        if (hasValidMaterials) {
            clearChildren();

            requiredTools = schematic.getRequiredToolLevels(targetStack, materials);

            requiredTools.forEach((tool, level) -> {
                ToolRequirementGui indicator = new ToolRequirementGui(getNumChildren() * GuiTool.width, 0, tool);
                indicator.updateRequirement(level, availableTools.getOrDefault(tool, 0));
                indicator.setAttachment(GuiAttachment.topRight);
                addChild(indicator);
            });
        }
    }

    public void updateAvailableTools(Map<ToolAction, Integer> availableTools) {
        getChildren(ToolRequirementGui.class).forEach(indicator ->
                indicator.updateRequirement(
                        requiredTools.getOrDefault(indicator.getToolType(), 0),
                        availableTools.getOrDefault(indicator.getToolType(), 0)));
    }
}
