package se.mickelus.tetra.items.toolbelt.module;

import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.items.toolbelt.ItemToolbeltModular;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.SingleVariantSchema;

public class SingleStrapModule extends ItemModuleMajor<ModuleDataToolbelt> {
    public static final String key = "toolbelt/single_strap";

    public static SingleStrapModule instance;

    public SingleStrapModule(String slotKey) {
        super(slotKey, key);

        data = DataHandler.instance.getModuleData(moduleKey, ModuleDataToolbelt[].class);
        instance = this;
        ItemUpgradeRegistry.instance.registerModule(moduleKey, this);
    }
}
