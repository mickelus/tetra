package se.mickelus.tetra.items.modular.impl.toolbelt.booster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class TickHandlerBooster {

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (TickEvent.Phase.START == event.phase) {
            ItemStack itemStack = ToolbeltHelper.findToolbelt(event.player);
            int level = UtilBooster.getBoosterLevel(itemStack);
            if (level > 0) {
                tickItem(event.player, itemStack, level);
            }
        }
    }

    public void tickItem(Player player, ItemStack stack, int level) {
        CompoundTag tag = stack.getOrCreateTag();
        boolean charged = tag.getBoolean(UtilBooster.chargedKey);
        if (!player.isInWater() && player.getVehicle() == null && UtilBooster.isActive(tag) && UtilBooster.hasFuel(tag, charged)) {
            if (charged) {
                UtilBooster.boostPlayerCharged(player, tag, level);
            } else {
                UtilBooster.boostPlayer(player, tag, level);
            }

            UtilBooster.consumeFuel(tag, charged);
        } else {
            UtilBooster.rechargeFuel(tag, stack);
        }

        if (charged) {
            tag.putBoolean(UtilBooster.chargedKey, false);
        }
    }
}
