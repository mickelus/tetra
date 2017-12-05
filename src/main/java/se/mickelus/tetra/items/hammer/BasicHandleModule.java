package se.mickelus.tetra.items.hammer;

import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.ModuleData;
import se.mickelus.tetra.module.RenderLayer;

public class BasicHandleModule extends ItemModuleMajor<ModuleData> {

    public static final String moduleKey = "hammer/basic_handle";

    public static BasicHandleModule instance;

    public BasicHandleModule(String slotKey) {
        super(slotKey, moduleKey);

        data = DataHandler.instance.getModuleData(moduleKey, ModuleData[].class);

        renderLayer = RenderLayer.LOWER;

        instance = this;
        ItemUpgradeRegistry.instance.registerModule(moduleKey, this);
    }
}
