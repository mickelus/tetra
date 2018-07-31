package se.mickelus.tetra.items;

import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.ModuleData;

public class BasicModule extends ItemModule<ModuleData> {
    public BasicModule(String slotKey, String moduleKey) {
        super(slotKey, moduleKey);

        data = DataHandler.instance.getModuleData(moduleKey, ModuleData[].class);
        ItemUpgradeRegistry.instance.registerModule(moduleKey, this);
    }
}
