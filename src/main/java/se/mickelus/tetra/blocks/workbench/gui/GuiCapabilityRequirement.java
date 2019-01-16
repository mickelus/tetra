package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.gui.*;

import java.util.Collections;
import java.util.List;

public class GuiCapabilityRequirement extends GuiCapability {

    int requiredLevel;
    int availableLevel;

    public GuiCapabilityRequirement(int x, int y, Capability capability) {
        super(x, y, capability);
    }

    public void updateRequirement(int requiredLevel, int availableLevel) {
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
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus() && requiredLevel > availableLevel) {
            return Collections.singletonList(I18n.format(capability + ".unavailable", requiredLevel));
        }
        return super.getTooltipLines();
    }
}
