package se.mickelus.tetra.items.modular.impl.toolbelt.booster;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;
import se.mickelus.tetra.util.ItemStackTagHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TickHandlerBooster {

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerTick(PlayerTickEvent.Pre event) {
        ItemStack itemStack = ToolbeltHelper.findToolbelt(event.getEntity());
        int level = UtilBooster.getBoosterLevel(itemStack);
        if (level > 0) {
            tickItem(event.getEntity(), itemStack, level);
        }
    }

    public void tickItem(Player player, ItemStack stack, int level) {
        ItemStackTagHelper.mutate(stack, tag -> {
            boolean charged = tag.getBoolean(UtilBooster.chargedKey);
            if (!player.isInWater() && player.getVehicle() == null && UtilBooster.isActive(tag) && UtilBooster.hasFuel(tag, charged)) {
                if (charged) {
                    UtilBooster.boostPlayerCharged(player, tag, level);
                } else {
                    UtilBooster.boostPlayer(player, tag, level);
                }

                UtilBooster.consumeFuel(tag, charged);
            } else {
                UtilBooster.rechargeFuel(tag, stack, player.registryAccess());
            }

            if (charged) {
                tag.putBoolean(UtilBooster.chargedKey, false);
            }
        });
    }
}
