package se.mickelus.tetra.items.toolbelt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.UUID;

public class TickHandlerToolbelt {

    private HashMap<UUID, ItemStack> previousOffhandMap = new HashMap<UUID, ItemStack>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side != Side.SERVER) {
            return;
        }

        InventoryToolbelt inventory = UtilToolbelt.findToolbeltInventory(event.player);

        if (inventory == null) {
            return;
        }

        restockToolbelt(event.player, inventory);

        storeOffhand(event.player);
    }

    public void restockToolbelt(EntityPlayer player, InventoryToolbelt toolbeltInventory) {

        for (int i = 0; i < toolbeltInventory.getSizeInventory(); i++) {
            ItemStack shadowStack = toolbeltInventory.getShadowOfSlot(i);
            if (!toolbeltInventory.getStackInSlot(i).isEmpty() || shadowStack.isEmpty()) {
                continue;
            }

            if (!wasInOffhand(player, toolbeltInventory.getShadowOfSlot(i))) {
                continue;
            }

            ItemStack inventoryStack = getStackFromPlayer(player, shadowStack);
            if (!inventoryStack.isEmpty()) {
                toolbeltInventory.setInventorySlotContents(i, inventoryStack);
            }
        }
    }

    private ItemStack getStackFromPlayer(EntityPlayer player, ItemStack shadowStack) {
        InventoryPlayer playerInventory = player.inventory;

        if (shadowStack.isItemEqual(playerInventory.getCurrentItem())) {
            return playerInventory.removeStackFromSlot(playerInventory.currentItem);
        }

        return ItemStack.EMPTY;
    }

    private void storeOffhand(EntityPlayer player) {
        previousOffhandMap.put(player.getUniqueID(), player.getHeldItemOffhand());
    }

    private boolean wasInOffhand(EntityPlayer player, ItemStack itemStack) {
        ItemStack previousOffhand = previousOffhandMap.get(player.getUniqueID());

        if (previousOffhand == null || previousOffhand.isEmpty()) {
            return false;
        }

        if (previousOffhand.isItemEqual(player.getHeldItemOffhand())) {
            return false;
        }

        if (!previousOffhand.isItemEqual(itemStack)) {
            return false;
        }

        return true;
    }

}
