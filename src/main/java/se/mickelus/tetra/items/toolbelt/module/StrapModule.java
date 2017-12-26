package se.mickelus.tetra.items.toolbelt.module;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.items.toolbelt.InventoryToolbelt;
import se.mickelus.tetra.items.toolbelt.UtilToolbelt;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

public class StrapModule extends ItemModuleMajor<ModuleDataToolbelt> {
    public StrapModule(String slotKey, String moduleKey) {
        super(slotKey, moduleKey);

        data = DataHandler.instance.getModuleData(moduleKey, ModuleDataToolbelt[].class);
        ItemUpgradeRegistry.instance.registerModule(moduleKey, this);
    }

    @Override
    public void postRemove(ItemStack targetStack, EntityPlayer player) {
        UtilToolbelt.emptyOverflowSlots(targetStack, player);
    }
}
