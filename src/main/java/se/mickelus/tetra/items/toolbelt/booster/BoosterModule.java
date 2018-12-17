package se.mickelus.tetra.items.toolbelt.booster;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.data.DataHandler;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.MultiSlotModule;
import se.mickelus.tetra.module.data.ModuleData;

public class BoosterModule extends MultiSlotModule<ModuleData> {
    public BoosterModule(String slotKey, String moduleKey, String slotSuffix) {
        super(slotKey, moduleKey, slotSuffix);

        // this uses the unsuffixed module key, to use the same data for both sides
        data = DataHandler.instance.getModuleData(moduleKey, ModuleData[].class);

        // this uses the suffixed module key, to avoid passing the slot key to every method that makes use of data
        ItemUpgradeRegistry.instance.registerModule(this.moduleKey, this);
    }

    @Override
    public ResourceLocation[] getTextures(ItemStack itemStack) {
        return new ResourceLocation[0];
    }

    @Override
    public ResourceLocation[] getAllTextures() {
        return new ResourceLocation[0];
    }

}
