package se.mickelus.tetra.crafting;

import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.items.modular.IModularItem;

public class GrindstoneMergeHandler {
    @SubscribeEvent
    public static void onGrindstoneUse(GrindstoneEvent.OnPlaceItem event) {
        if (event.getBottomItem().getItem() instanceof IModularItem && event.getTopItem().getItem() instanceof IModularItem) {
            event.setCanceled(true);
        }
    }
}
