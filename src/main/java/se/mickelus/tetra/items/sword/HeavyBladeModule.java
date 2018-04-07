package se.mickelus.tetra.items.sword;

import org.apache.commons.lang3.ArrayUtils;
import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.HandheldModuleData;
import se.mickelus.tetra.module.data.ImprovementData;

public class HeavyBladeModule extends ItemModuleMajor<HandheldModuleData> {

    public static final String key = "sword/heavy_blade";

    public static HeavyBladeModule instance;

    public HeavyBladeModule(String slotKey) {
        super(slotKey, key);

        data = DataHandler.instance.getModuleData(key, HandheldModuleData[].class);

        improvements = ArrayUtils.addAll(
                DataHandler.instance.getModuleData("sword/improvements/blade_enchants", ImprovementData[].class),
                DataHandler.instance.getModuleData("sword/improvements/heavy_blade", ImprovementData[].class)
        );

        instance = this;
        ItemUpgradeRegistry.instance.registerModule(key, this);
    }
}