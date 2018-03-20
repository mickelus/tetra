package se.mickelus.tetra.items.toolbelt.module;

import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.items.toolbelt.ItemToolbeltModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.ModuleData;
import se.mickelus.tetra.module.schema.SingleVariantSchema;

public class BeltModule extends ItemModule {
    public static final String key = "toolbelt/belt";

    public static BeltModule instance;

    public BeltModule(String slotKey) {
        super(slotKey, key);

        data = DataHandler.instance.getModuleData(moduleKey, ModuleData[].class);
        instance = this;
        ItemUpgradeRegistry.instance.registerModule(moduleKey, this);
    }

    public void registerUpgradeSchemas() {
        for (int i = 0; i < data.length - 1; i++) {
            new SingleVariantSchema("toolbelt_belt" + i, this, ItemToolbeltModular.instance,
                data[i].key, data[i+1].key);
        }
    }
}
