package se.mickelus.tetra.items.sword;

import org.apache.commons.lang3.ArrayUtils;
import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.module.*;
import se.mickelus.tetra.module.data.HandheldModuleData;
import se.mickelus.tetra.module.data.ImprovementData;

public class ShortBladeModule extends ItemModuleMajor<HandheldModuleData> {

    public static final String key = "sword/short_blade";

    public static final String hookedImprovement = "short_blade/hooked";
    public static final String temperedImprovement = "short_blade/tempered";
    public static final String serratedImprovement = "short_blade/serrated";

    public static ShortBladeModule instance;

    public ShortBladeModule(String slotKey) {
        super(slotKey, key);

        data = DataHandler.instance.getModuleData(key, HandheldModuleData[].class);

        improvements = ArrayUtils.addAll(
                DataHandler.instance.getModuleData("sword/improvements/blade_enchants", ImprovementData[].class),
                DataHandler.instance.getModuleData("sword/improvements/short_blade", ImprovementData[].class)
        );

        instance = this;
        ItemUpgradeRegistry.instance.registerModule(key, this);
    }
}
