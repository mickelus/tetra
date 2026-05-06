package se.mickelus.tetra.items.forged;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class VibrationDebuffer {
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!event.getEntity().level().isClientSide && event.getEntity().level().getGameTime() % 20 == 0
                && hasApplicableItem(event.getEntity())) {
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 1));
        }
    }

    private boolean hasApplicableItem(Player player) {
        Item mainHandItem = player.getMainHandItem().getItem();
        Item offHandItem = player.getOffhandItem().getItem();
        return EarthpiercerItem.instance.equals(mainHandItem) || EarthpiercerItem.instance.equals(offHandItem)
                || StonecutterItem.instance != null && (StonecutterItem.instance.equals(mainHandItem) || StonecutterItem.instance.equals(offHandItem));
    }
}
