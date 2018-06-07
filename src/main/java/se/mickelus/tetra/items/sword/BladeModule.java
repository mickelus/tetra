package se.mickelus.tetra.items.sword;

import org.apache.commons.lang3.ArrayUtils;
import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.HandheldModuleData;
import se.mickelus.tetra.module.data.ImprovementData;

import java.util.stream.Stream;

public class BladeModule extends ItemModuleMajor<HandheldModuleData> {

    public static final String key = "sword/basic_blade";

    public static BladeModule instance;

    public BladeModule(String slotKey) {
        super(slotKey, key);

        data = DataHandler.instance.getModuleData(key, HandheldModuleData[].class);

        improvements = Stream.of(
                DataHandler.instance.getImprovementData("shared_improvements"),
                DataHandler.instance.getImprovementData("sword/improvements/blade_enchants"),
                DataHandler.instance.getImprovementData("sword/improvements/basic_blade")
        )
                .flatMap(Stream::of)
                .toArray(ImprovementData[]::new);

        instance = this;
        ItemUpgradeRegistry.instance.registerModule(key, this);
    }
}
