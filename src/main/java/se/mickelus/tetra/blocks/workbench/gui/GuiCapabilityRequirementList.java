package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Collections;
import java.util.Map;

public class GuiCapabilityRequirementList extends GuiElement {

    private Map<ToolType, Integer> requiredTools = Collections.emptyMap();

    public GuiCapabilityRequirementList(int x, int y) {
        super(x, y, 54, 18);
    }

    public void update(UpgradeSchematic schematic, ItemStack targetStack, String slot, ItemStack[] materials, Map<ToolType, Integer> availableTools) {
        boolean hasValidMaterials = schematic.isMaterialsValid(targetStack, slot, materials);
        setVisible(hasValidMaterials);

        if (hasValidMaterials) {
            clearChildren();

            requiredTools = schematic.getRequiredToolLevels(targetStack, materials);

            requiredTools.forEach((tool, level) -> {
                GuiCapabilityRequirement indicator = new GuiCapabilityRequirement(getNumChildren() * GuiTool.width, 0, tool);
                indicator.updateRequirement(level, availableTools.getOrDefault(tool, 0));
                indicator.setAttachment(GuiAttachment.topRight);
                addChild(indicator);
            });
        }
    }

    public void updateAvailableCapabilities(Map<ToolType, Integer> availableTools) {
        getChildren(GuiCapabilityRequirement.class).forEach(indicator ->
                indicator.updateRequirement(
                        requiredTools.getOrDefault(indicator.getToolType(), 0),
                        availableTools.getOrDefault(indicator.getToolType(), 0)));
    }
}
