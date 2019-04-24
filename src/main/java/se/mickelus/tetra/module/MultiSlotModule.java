package se.mickelus.tetra.module;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataHandler;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.ModuleData;

import java.util.Arrays;

public class MultiSlotModule<T extends ModuleData> extends ItemModuleMajor<T> {

    protected String slotSuffix;

    protected String unlocalizedName;

    public MultiSlotModule(String slotKey, String moduleKey, String slotSuffix, String ... improvementKeys) {
        super(slotKey, moduleKey + slotSuffix);

        this.slotSuffix = slotSuffix;

        this.unlocalizedName = moduleKey;

        this.dataKey = moduleKey + slotSuffix + "_material";

        if (improvementKeys.length > 0) {
            improvements = Arrays.stream(improvementKeys)
                    .map(key -> DataHandler.instance.getModuleData(key, ImprovementData[].class))
                    .flatMap(Arrays::stream)
                    .toArray(ImprovementData[]::new);
        }
    }

    @Override
    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public ResourceLocation[] getAllTextures() {
        return Arrays.stream(data)
                .map(moduleData -> moduleData.key)
                .map(key -> "items/" + key + slotSuffix)
                .map(key -> new ResourceLocation(TetraMod.MOD_ID, key))
                .toArray(ResourceLocation[]::new);
    }

    public ResourceLocation[] getTextures(ItemStack itemStack) {
        String string = "items/" + getData(itemStack).key + slotSuffix;
        return new ResourceLocation[] { new ResourceLocation(TetraMod.MOD_ID, string)};
    }
}
