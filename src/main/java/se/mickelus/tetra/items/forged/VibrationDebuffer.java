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
        if (!event.player.level.isClientSide && event.player.level.getGameTime() % 20 == 0
                && hasApplicableItem(event.player)) {
            event.player.addEffect(new EffectInstance(Effects.CONFUSION, 80, 1));
        }
    }

    private boolean hasApplicableItem(PlayerEntity player) {
        Item mainHandItem = player.getMainHandItem().getItem();
        Item offHandItem = player.getOffhandItem().getItem();
        return EarthpiercerItem.instance.equals(mainHandItem) || EarthpiercerItem.instance.equals(offHandItem)
                || StonecutterItem.instance != null && (StonecutterItem.instance.equals(mainHandItem) || StonecutterItem.instance.equals(offHandItem));
    }
}
