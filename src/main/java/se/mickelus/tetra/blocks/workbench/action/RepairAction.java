package se.mickelus.tetra.blocks.workbench.action;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.blocks.workbench.TileEntityWorkbench;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.schema.RepairSchema;
import se.mickelus.tetra.module.schema.UpgradeSchema;

import java.util.Arrays;

public class RepairAction implements WorkbenchAction {

    public static final String key = "repair_action";

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean canPerformOn(EntityPlayer player, ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemModular) {
            UpgradeSchema[] schemas = ItemUpgradeRegistry.instance.getAvailableSchemas(player, itemStack);
            return Arrays.stream(schemas)
                    .filter(upgradeSchema -> upgradeSchema.isApplicableForSlot(null, itemStack))
                    .anyMatch(upgradeSchema -> upgradeSchema instanceof RepairSchema);
        }

        return false;
    }

    @Override
    public Capability[] getRequiredCapabilitiesFor(ItemStack itemStack) {
        return new Capability[0];
    }

    @Override
    public int getCapabilityLevel(ItemStack itemStack, Capability capability) {
        return 0;
    }

    @Override
    public void perform(EntityPlayer player, ItemStack itemStack, TileEntityWorkbench workbench) {
        UpgradeSchema[] schemas = ItemUpgradeRegistry.instance.getAvailableSchemas(player, itemStack);
        Arrays.stream(schemas)
                .filter(upgradeSchema -> upgradeSchema.isApplicableForSlot(null, itemStack))
                .filter(upgradeSchema -> upgradeSchema instanceof RepairSchema)
                .findFirst()
                .ifPresent(upgradeSchema -> workbench.setCurrentSchema(upgradeSchema, null));
    }
}
