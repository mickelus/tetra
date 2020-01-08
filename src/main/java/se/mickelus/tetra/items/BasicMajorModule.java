package se.mickelus.tetra.items;

import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.Priority;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.ModuleData;
import se.mickelus.tetra.util.Filter;

import java.util.Arrays;
import java.util.Objects;

public class BasicMajorModule extends ItemModuleMajor {
    public BasicMajorModule(String slotKey, String moduleKey, String ... improvementKeys) {
        super(slotKey, moduleKey);

        DataManager.moduleData.onReload(() -> data = DataManager.moduleData.getData(new ResourceLocation(TetraMod.MOD_ID, moduleKey)));

        if (improvementKeys.length > 0) {
            DataManager.improvementData.onReload(() -> {
                improvements = Arrays.stream(improvementKeys)
                        .map(key -> DataManager.improvementData.getData(new ResourceLocation(TetraMod.MOD_ID, key)))
                        .filter(Objects::nonNull)
                        .flatMap(Arrays::stream)
                        .filter(Filter.distinct(improvement -> improvement.key + ":" + improvement.level))
                        .toArray(ImprovementData[]::new);


                settleMax = Arrays.stream(improvements)
                        .filter(data -> data.key.equals(settleImprovement))
                        .mapToInt(ImprovementData::getLevel)
                        .max()
                        .orElse(0);
            });
        }

        ItemUpgradeRegistry.instance.registerModule(moduleKey, this);
    }

    public BasicMajorModule withRenderLayer(Priority layer) {
        this.renderLayer = layer;
        return this;
    }
}
