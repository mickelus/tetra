package se.mickelus.tetra.items;

import se.mickelus.tetra.data.DataHandler;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.ModuleData;
import se.mickelus.tetra.module.data.TweakData;

public class BasicModule extends ItemModule<ModuleData> {
    public BasicModule(String slotKey, String moduleKey) {
        super(slotKey, moduleKey);

        data = DataHandler.instance.getModuleData(moduleKey, ModuleData[].class);
        ItemUpgradeRegistry.instance.registerModule(moduleKey, this);
    }

    public BasicModule(String slotKey, String moduleKey, String tweakKey) {
        this(slotKey, moduleKey);

        tweaks = DataHandler.instance.getModuleData(tweakKey, TweakData[].class);
    }
}
