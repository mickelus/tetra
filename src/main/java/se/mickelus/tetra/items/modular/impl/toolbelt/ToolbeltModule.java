package se.mickelus.tetra.items.modular.impl.toolbelt;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.module.MultiSlotMajorModule;
import se.mickelus.tetra.module.data.ModuleData;

public class ToolbeltModule extends MultiSlotMajorModule {
    public ToolbeltModule(ResourceLocation identifier, ModuleData data) {
        super(identifier, data);
    }

    @Override
    public void postRemove(ItemStack targetStack, Player player) {
        ToolbeltHelper.emptyOverflowSlots(targetStack, player);
    }
}
