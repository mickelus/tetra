package se.mickelus.tetra.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.neoforged.bus.api.SubscribeEvent;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.items.modular.IModularItem;

public class GrindstoneMergeHandler {
    @SubscribeEvent
    public static void onGrindstoneUse(GrindstoneEvent.OnPlaceItem event) {
        if (event.getBottomItem().getItem() instanceof IModularItem && event.getTopItem().getItem() instanceof IModularItem) {
            event.setCanceled(true);
            return;
        }

        ItemStack itemStack = event.getTopItem().isEmpty() ? event.getBottomItem() : event.getTopItem();
        ItemStack otherStack = event.getTopItem().isEmpty() ? event.getTopItem() : event.getBottomItem();
        if (itemStack.getItem() instanceof IModularItem && otherStack.isEmpty() && EnchantmentHelper.hasAnyEnchantments(itemStack)) {
            event.setOutput(TetraEnchantmentHelper.removeAllEnchantments(itemStack.copy()));
        }
    }
}
