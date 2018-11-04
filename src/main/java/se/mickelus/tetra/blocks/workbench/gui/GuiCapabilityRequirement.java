package se.mickelus.tetra.blocks.workbench.gui;

import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.gui.*;

public class GuiCapabilityRequirement extends GuiCapability {

    public GuiCapabilityRequirement(int x, int y, Capability capability) {
        super(x, y, capability);
    }

    public void updateRequirement(int requiredLevel, int availableLevel) {
        setVisible(requiredLevel != 0);

        if (requiredLevel > availableLevel) {
            update(requiredLevel, GuiColors.remove);
        } else {
            update(requiredLevel, GuiColors.add);
        }
    }
}
