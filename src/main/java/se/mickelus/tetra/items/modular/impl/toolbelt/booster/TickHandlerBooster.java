package se.mickelus.tetra.items.modular.impl.toolbelt.booster;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.util.NBTHelper;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;

public class TickHandlerBooster {

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        ItemStack itemStack = ToolbeltHelper.findToolbelt(event.player);
        int level = UtilBooster.getBoosterLevel(itemStack);
        if (level > 0) {
            tickItem(event.player, itemStack, level);
        }
    }

    public void tickItem(PlayerEntity player, ItemStack stack, int level) {
        CompoundNBT tag = NBTHelper.getTag(stack);
        boolean charged = tag.getBoolean(UtilBooster.chargedKey);
        if (!player.isInWater() && player.getRidingEntity() == null && UtilBooster.isActive(tag) && UtilBooster.hasFuel(tag, charged)) {
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
