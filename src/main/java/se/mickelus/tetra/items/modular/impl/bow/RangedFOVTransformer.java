package se.mickelus.tetra.items.modular.impl.bow;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.util.CastOptional;

public class RangedFOVTransformer {

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onFOVUpdate(FOVUpdateEvent event) {
        PlayerEntity player = event.getEntity();
        if (player.isHandActive()) {
            ItemStack itemStack = player.getActiveItemStack();
            CastOptional.cast(itemStack.getItem(), ModularBowItem.class)
                    .ifPresent(item -> {
                        float progress = item.getProgress(itemStack, player);
                        if (progress > 1.0F) {
                            progress = 1.0F;
                        } else {
                            progress = progress * progress;
                        }

                        event.setNewfov((event.getNewfov() * 1.0F - progress * 0.15F) / getZoom(item, itemStack));
                    });
        } else if (player.isCrouching()) {
            ItemStack itemStack = player.getHeldItemMainhand();
            CastOptional.cast(itemStack.getItem(), ModularBowItem.class)
                    .ifPresent(item -> event.setNewfov(event.getNewfov() / getZoom(item, itemStack)));
        }
    }

    private float getZoom(ModularItem item, ItemStack itemStack) {
        return Math.max(1, item.getEffectLevel(itemStack, ItemEffect.zoom) / 10f);
    }
}
