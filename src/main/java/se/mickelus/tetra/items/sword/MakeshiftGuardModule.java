package se.mickelus.tetra.items.sword;

import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.ModuleData;

public class MakeshiftGuardModule extends ItemModule<ModuleData> {
    public static final String key = "sword/makeshift_guard";
    public static MakeshiftGuardModule instance;
    public MakeshiftGuardModule(String slotKey) {
        super(slotKey, key);

        data = DataHandler.instance.getModuleData(moduleKey, ModuleData[].class);
        ItemUpgradeRegistry.instance.registerModule(moduleKey, this);

        instance = this;
    }
}
