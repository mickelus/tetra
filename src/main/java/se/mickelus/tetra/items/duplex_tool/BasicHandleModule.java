package se.mickelus.tetra.items.duplex_tool;

import se.mickelus.tetra.data.DataHandler;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.ModuleData;
import se.mickelus.tetra.module.Priority;

public class BasicHandleModule extends ItemModule<ModuleData> {

    public static final String moduleKey = "duplex/basic_handle";

    public static BasicHandleModule instance;

    public BasicHandleModule(String slotKey) {
        super(slotKey, moduleKey);

        data = DataHandler.instance.getModuleData(moduleKey, ModuleData[].class);

        renderLayer = Priority.LOWER;

        instance = this;
        ItemUpgradeRegistry.instance.registerModule(moduleKey, this);
    }
}
