package se.mickelus.tetra.items.forged;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VibrationDebuffer {
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!event.player.world.isRemote && event.player.world.getGameTime() % 20 == 0
                && hasApplicableItem(event.player)) {
            event.player.addPotionEffect(new EffectInstance(Effects.NAUSEA, 80, 1));
        }
    }

    private boolean hasApplicableItem(PlayerEntity player) {
        Item mainHandItem = player.getHeldItemMainhand().getItem();
        Item offHandItem = player.getHeldItemOffhand().getItem();
        return EarthpiercerItem.instance.equals(mainHandItem) || EarthpiercerItem.instance.equals(offHandItem)
                || StonecutterItem.instance != null && (StonecutterItem.instance.equals(mainHandItem) || StonecutterItem.instance.equals(offHandItem));
    }
}
