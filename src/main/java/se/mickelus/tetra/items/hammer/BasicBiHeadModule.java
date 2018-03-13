package se.mickelus.tetra.items.hammer;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.ModuleData;

import java.util.Arrays;

public class BasicBiHeadModule extends ItemModuleMajor<ModuleData> {

    protected String sideSuffix;

    public BasicBiHeadModule(String slotKey, String moduleKey, boolean isLeft) {
        super(slotKey, moduleKey + (isLeft ? "_left" : "_right"));

        if (isLeft) {
            sideSuffix = "_left";
        } else {
            sideSuffix = "_right";
        }

        this.dataKey = moduleKey + sideSuffix + "_material";

        // this uses the unsuffixed module key, to use the same data for both sides
        data = DataHandler.instance.getModuleData(moduleKey, ModuleData[].class);

        // this uses the suffixed module key, to avoid passing the slot key to every method that makes use of data
        ItemUpgradeRegistry.instance.registerModule(this.moduleKey, this);
    }

    public ResourceLocation[] getAllTextures() {
        return Arrays.stream(data)
                .map(moduleData -> moduleData.key)
                .map(key -> "items/" + key + sideSuffix)
                .map(key -> new ResourceLocation(TetraMod.MOD_ID, key))
                .toArray(ResourceLocation[]::new);
    }

    public ResourceLocation[] getTextures(ItemStack itemStack) {
        String string = "items/" + getData(itemStack).key + sideSuffix;
        return new ResourceLocation[] { new ResourceLocation(TetraMod.MOD_ID, string)};
    }
}
