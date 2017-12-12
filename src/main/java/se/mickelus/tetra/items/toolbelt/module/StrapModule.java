package se.mickelus.tetra.items.toolbelt.module;

import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

public class StrapModule extends ItemModuleMajor<ModuleDataToolbelt> {
    public StrapModule(String slotKey, String moduleKey) {
        super(slotKey, moduleKey);

        data = DataHandler.instance.getModuleData(moduleKey, ModuleDataToolbelt[].class);
        ItemUpgradeRegistry.instance.registerModule(moduleKey, this);
    }
}
