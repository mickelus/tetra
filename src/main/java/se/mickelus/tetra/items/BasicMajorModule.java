package se.mickelus.tetra.items;

import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.ModuleData;

import java.util.Arrays;

public class BasicMajorModule extends ItemModuleMajor<ModuleData> {
    public BasicMajorModule(String slotKey, String moduleKey, String ... improvementKeys) {
        super(slotKey, moduleKey);

        data = DataHandler.instance.getModuleData(moduleKey, ModuleData[].class);

        if (improvementKeys.length > 0) {
            improvements = Arrays.stream(improvementKeys)
                    .map(key -> DataHandler.instance.getModuleData(key, ImprovementData[].class))
                    .flatMap(Arrays::stream)
                    .toArray(ImprovementData[]::new);
        }

        ItemUpgradeRegistry.instance.registerModule(moduleKey, this);
    }
}
