package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.blocks.workbench.TileEntityWorkbench;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.gui.GuiAttachment;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.module.schema.UpgradeSchema;

import java.util.Collection;

public class GuiCapabilityRequirementList extends GuiElement {

    private GuiCapabilityRequirement[] indicators;

    public GuiCapabilityRequirementList(int x, int y) {
        super(x, y, 54, 18);

        Capability[] capabilities = Capability.values();
        indicators = new GuiCapabilityRequirement[capabilities.length];
        for (int i = 0; i < capabilities.length; i++) {
            indicators[i] = new GuiCapabilityRequirement(0, 0, capabilities[i]);
            indicators[i].setAttachment(GuiAttachment.topRight);
            addChild(indicators[i]);
        }
    }

    public void update(UpgradeSchema schema, ItemStack targetStack, ItemStack[] materials, int[] availableCapabilities) {
        setVisible(schema.isMaterialsValid(targetStack, materials));

        int visibleCount = 0;
        Capability[] capabilities = Capability.values();
        Collection<Capability> requiredCapabilities = schema.getRequiredCapabilities(targetStack, materials);
        for (int i = 0; i < capabilities.length; i++) {
            int requiredLevel = schema.getRequiredCapabilityLevel(targetStack, materials, capabilities[i]);
            if (requiredCapabilities.contains(capabilities[i]) && requiredLevel > 0) {
                indicators[i].setX(-visibleCount * indicators[i].getWidth());
                indicators[i].updateRequirement(requiredLevel, availableCapabilities[i]);
                indicators[i].setVisible(true);
                visibleCount++;
            } else {
                indicators[i].setVisible(false);
            }
        }
    }
}
