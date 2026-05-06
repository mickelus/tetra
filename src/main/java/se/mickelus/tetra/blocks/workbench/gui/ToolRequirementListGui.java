package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbility;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.impl.GuiHorizontalLayoutGroup;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@ParametersAreNonnullByDefault
public class ToolRequirementListGui extends GuiElement {

    private Map<ItemAbility, Integer> requiredTools = Collections.emptyMap();

    public ToolRequirementListGui(int x, int y) {
        super(x, y, 0, 0);
        setAttachmentPoint(GuiAttachment.topCenter);
    }

    public void update(UpgradeSchematic schematic, ItemStack targetStack, String slot, ItemStack[] materials, Map<ItemAbility, Integer> availableTools) {
        boolean hasValidMaterials = schematic.isMaterialsValid(targetStack, slot, materials);
        setVisible(hasValidMaterials);

        if (hasValidMaterials) {
            clearChildren();

            requiredTools = schematic.getRequiredToolLevels(targetStack, materials);

            var layout = new GuiHorizontalLayoutGroup(0, 0, 16, -2 - requiredTools.size() * 2);
            layout.setAttachmentPoint(GuiAttachment.topCenter);
            addChild(layout);
            var spacing = -3 - requiredTools.size() * 2;

            var i = new AtomicInteger(0);
            requiredTools.entrySet().stream()
                    .map(entry -> new ToolRequirementGui(-1 * i.getAndIncrement() * (spacing + GuiTool.width), 0, entry.getKey())
                            .updateRequirement(entry.getValue(), availableTools.getOrDefault(entry.getKey(), 0))
                            .setAttachment(GuiAttachment.topRight))
                    .forEach(this::addChild);
            setWidth(requiredTools.size() * GuiTool.width + (requiredTools.size() - 1) * spacing);
        }
    }

    public void updateAvailableTools(Map<ItemAbility, Integer> availableTools) {
        getChildren(ToolRequirementGui.class).forEach(indicator ->
                indicator.updateRequirement(
                        requiredTools.getOrDefault(indicator.getItemAbility(), 0),
                        availableTools.getOrDefault(indicator.getItemAbility(), 0)));
    }
}
