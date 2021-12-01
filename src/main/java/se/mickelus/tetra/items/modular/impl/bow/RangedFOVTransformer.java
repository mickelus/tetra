package se.mickelus.tetra.items.modular.impl.bow;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.FOVModifierEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class RangedFOVTransformer {

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onFOVUpdate(FOVModifierEvent event) {
        Player player = event.getEntity();
        if (player.isUsingItem()) {
            ItemStack itemStack = player.getUseItem();
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
            ItemStack itemStack = player.getMainHandItem();
            CastOptional.cast(itemStack.getItem(), ModularBowItem.class)
                    .ifPresent(item -> event.setNewfov(event.getNewfov() / getZoom(item, itemStack)));
        }
    }

    private float getZoom(IModularItem item, ItemStack itemStack) {
        return Math.max(1, item.getEffectLevel(itemStack, ItemEffect.zoom) / 10f);
    }
}
