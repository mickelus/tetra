package se.mickelus.tetra.items.sword;

import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.ModuleData;

public class DecorativePommelModule extends ItemModule<ModuleData> {
    public static final String key = "sword/decorative_pommel";
    public static DecorativePommelModule instance;
    public DecorativePommelModule(String slotKey) {
        super(slotKey, key);


        data = DataHandler.instance.getModuleData(moduleKey, ModuleData[].class);
        ItemUpgradeRegistry.instance.registerModule(moduleKey, this);

        instance = this;
    }
}
