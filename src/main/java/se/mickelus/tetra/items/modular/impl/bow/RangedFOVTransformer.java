package se.mickelus.tetra.items.modular.impl.bow;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RangedFOVTransformer {

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onFOVUpdate(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
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

                        event.setNewFovModifier((event.getNewFovModifier() - progress * 0.15F));
                    });
        }

        if (player.isCrouching()) {
            ItemStack itemStack = player.getMainHandItem();
            CastOptional.cast(itemStack.getItem(), ModularBowItem.class)
                    .ifPresent(item -> event.setNewFovModifier(event.getNewFovModifier() / getZoom(item, itemStack)));
        }
    }

    private float getZoom(IModularItem item, ItemStack itemStack) {
        return Math.max(1, item.getEffectLevel(itemStack, ItemEffect.zoom) / 10f);
    }
}
