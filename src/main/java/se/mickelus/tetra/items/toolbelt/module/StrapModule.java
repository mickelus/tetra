package se.mickelus.tetra.items.toolbelt.module;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.items.toolbelt.UtilToolbelt;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.ModuleData;
import se.mickelus.tetra.module.MultiSlotModule;

public class StrapModule extends MultiSlotModule {
    public StrapModule(String slotKey, String moduleKey, String slotSuffix) {
        super(slotKey, moduleKey, slotSuffix);

        // this uses the unsuffixed module key, to use the same data for both sides
        data = DataHandler.instance.getModuleData(moduleKey, ModuleData[].class);

        // this uses the suffixed module key, to avoid passing the slot key to every method that makes use of data
        ItemUpgradeRegistry.instance.registerModule(this.moduleKey, this);
    }

    @Override
    public void postRemove(ItemStack targetStack, EntityPlayer player) {
        UtilToolbelt.emptyOverflowSlots(targetStack, player);
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
