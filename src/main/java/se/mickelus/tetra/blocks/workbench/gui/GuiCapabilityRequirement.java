package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.schema.UpgradeSchema;

public class GuiCapabilityRequirement extends GuiCapability {

    public GuiCapabilityRequirement(int x, int y, Capability capability) {
        super(x, y, capability);
    }

    public void update(EntityPlayer player, UpgradeSchema schema, ItemStack targetStack, ItemStack[] materials) {
        this.update(player, schema.getRequiredCapabilityLevel(targetStack, materials, capability));
    }

    public void update(EntityPlayer player, int requiredLevel) {
        setVisible(requiredLevel != 0);

        if (requiredLevel > CapabilityHelper.getCapabilityLevel(player, capability)) {
            update(requiredLevel, GuiColors.remove);
        } else {
            update(requiredLevel, GuiColors.add);
        }
    }
}
