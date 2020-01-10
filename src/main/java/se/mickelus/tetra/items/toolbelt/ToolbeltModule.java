package se.mickelus.tetra.items.toolbelt;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.module.MultiSlotModule;

public class ToolbeltModule extends MultiSlotModule {
    public ToolbeltModule(String slotKey, String moduleKey, String slotSuffix, String ... improvementKeys) {
        super(slotKey, moduleKey, slotSuffix, improvementKeys);
    }

    @Override
    public void postRemove(ItemStack targetStack, PlayerEntity player) {
        ToolbeltHelper.emptyOverflowSlots(targetStack, player);
    }

    @Override
    public ResourceLocation[] getTextures(ItemStack itemStack) {
        return new ResourceLocation[0];
    }
}
