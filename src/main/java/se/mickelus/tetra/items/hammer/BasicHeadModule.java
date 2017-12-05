package se.mickelus.tetra.items.hammer;

import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.ModuleData;

public class BasicHeadModule extends ItemModuleMajor<ModuleData> {

    public static final String moduleKey = "hammer/basic_head";

    public static BasicHeadModule instance;

    public BasicHeadModule(String slotKey) {
        super(slotKey, moduleKey);

        data = DataHandler.instance.getModuleData(moduleKey, ModuleData[].class);

        instance = this;
        ItemUpgradeRegistry.instance.registerModule(moduleKey, this);
    }
}
