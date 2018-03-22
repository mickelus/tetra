package se.mickelus.tetra.items.sword;

import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.module.*;

public class HiltModule extends ItemModuleMajor<HandheldModuleData> {

    public static final String key = "sword/basic_hilt";

    public static HiltModule instance;

    public HiltModule(String slotKey) {
        super(slotKey, key);

        data = DataHandler.instance.getModuleData(key, HandheldModuleData[].class);

        renderLayer = Priority.LOWER;

        instance = this;
        ItemUpgradeRegistry.instance.registerModule(key, this);
    }
}
