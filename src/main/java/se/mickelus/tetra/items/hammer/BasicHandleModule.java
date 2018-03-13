package se.mickelus.tetra.items.hammer;

import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.ModuleData;
import se.mickelus.tetra.module.Priority;

public class BasicHandleModule extends ItemModule<ModuleData> {

    public static final String moduleKey = "hammer/basic_handle";

    public static BasicHandleModule instance;

    public BasicHandleModule(String slotKey) {
        super(slotKey, moduleKey);

        data = DataHandler.instance.getModuleData(moduleKey, ModuleData[].class);

        renderLayer = Priority.LOWER;

        instance = this;
        ItemUpgradeRegistry.instance.registerModule(moduleKey, this);
    }
}
