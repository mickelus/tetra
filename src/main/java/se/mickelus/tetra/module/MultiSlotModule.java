package se.mickelus.tetra.module;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;

import java.util.Arrays;

public class MultiSlotModule extends ItemModuleMajor<ModuleData> {

    protected String slotSuffix;

    public MultiSlotModule(String slotKey, String moduleKey, String slotSuffix) {
        super(slotKey, moduleKey + slotSuffix);

        this.slotSuffix = slotSuffix;

        this.dataKey = moduleKey + slotSuffix + "_material";
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
