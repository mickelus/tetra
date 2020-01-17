package se.mickelus.tetra.items.toolbelt;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.module.MultiSlotModule;
import se.mickelus.tetra.module.data.ModuleData;

public class ToolbeltModule extends MultiSlotModule {
    public ToolbeltModule(ResourceLocation identifier, ModuleData data) {
        super(identifier, data);
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
