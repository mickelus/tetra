package se.mickelus.tetra.items.toolbelt;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.data.DataHandler;
import se.mickelus.tetra.items.toolbelt.UtilToolbelt;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.MultiSlotModule;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.ModuleData;

public class ToolbeltModule extends MultiSlotModule<ModuleData> {
    public ToolbeltModule(String slotKey, String moduleKey, String slotSuffix) {
        super(slotKey, "toolbelt/" + moduleKey, slotSuffix);

        // this uses the unsuffixed module key, to use the same data for both sides
        data = DataHandler.instance.getModuleData("toolbelt/" + moduleKey, ModuleData[].class);

        improvements = DataHandler.instance.getModuleData("toolbelt/improvements/" + moduleKey, ImprovementData[].class);

        // this uses the suffixed module key, to avoid passing the slot key to every method that makes use of data
        ItemUpgradeRegistry.instance.registerModule(this.moduleKey, this);
    }

    @Override
    public void postRemove(ItemStack targetStack, PlayerEntity player) {
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
