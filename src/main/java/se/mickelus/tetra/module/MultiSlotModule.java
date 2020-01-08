package se.mickelus.tetra.module;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.ModuleData;
import se.mickelus.tetra.util.Filter;

import java.util.Arrays;
import java.util.Objects;

public class MultiSlotModule extends ItemModuleMajor {

    protected String slotSuffix;

    protected String unlocalizedName;

    public MultiSlotModule(String slotKey, String moduleKey, String slotSuffix, String ... improvementKeys) {
        super(slotKey, moduleKey + slotSuffix);

        this.slotSuffix = slotSuffix;

        this.unlocalizedName = moduleKey;

        this.dataKey = moduleKey + slotSuffix + "_material";

        // this uses the unsuffixed module key, to use the same data for both sides
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

        // this uses the suffixed module key, to avoid passing the slot key to every method that makes use of data
        ItemUpgradeRegistry.instance.registerModule(this.moduleKey, this);
    }

    @Override
    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public ResourceLocation[] getAllTextures() {
        return Arrays.stream(data)
                .map(moduleData -> moduleData.key)
                .map(key -> "items/module/" + key + slotSuffix)
                .map(key -> new ResourceLocation(TetraMod.MOD_ID, key))
                .toArray(ResourceLocation[]::new);
    }

    public ResourceLocation[] getTextures(ItemStack itemStack) {
        String string = "items/module/" + getData(itemStack).key + slotSuffix;
        return new ResourceLocation[] { new ResourceLocation(TetraMod.MOD_ID, string)};
    }
}
