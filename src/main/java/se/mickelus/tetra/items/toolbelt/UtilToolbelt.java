package se.mickelus.tetra.items.toolbelt;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.items.toolbelt.inventory.*;
import se.mickelus.tetra.module.ItemEffect;

import java.util.Collection;
import java.util.List;

public class UtilToolbelt {
    public static void equipItemFromToolbelt(PlayerEntity player, ToolbeltSlotType slotType, int index, Hand hand) {
        InventoryToolbelt inventory = null;
        ItemStack toolbeltStack = findToolbelt(player);

        // stops things from crashing if the player has dropped the toolbelt stack after opening the overlay
        if (!(toolbeltStack.getItem() instanceof ItemToolbeltModular)) {
            return;
        }

        switch (slotType) {
            case quickslot:
                inventory = new InventoryQuickslot(toolbeltStack);
                break;
            case potion:
                inventory = new InventoryPotions(toolbeltStack);
                break;
            case quiver:
                inventory = new InventoryQuiver(toolbeltStack);
                break;
            case storage:
                inventory = new InventoryStorage(toolbeltStack);
                break;
        }

        if (inventory.getSizeInventory() <= index || inventory.getStackInSlot(index).isEmpty()) {
            return;
        }

        ItemStack heldItemStack = player.getHeldItem(hand);
        player.setHeldItem(hand, inventory.takeItemStack(index));


        if (!heldItemStack.isEmpty()) {
            if (!storeItemInToolbelt(toolbeltStack, heldItemStack)) {
                if (!player.inventory.addItemStackToInventory(heldItemStack)) {
                    inventory.storeItemInInventory(player.getHeldItem(hand));
                    player.setHeldItem(hand, heldItemStack);
                    player.sendStatusMessage(new TranslationTextComponent("toolbelt.blocked"), true);
                }
            }
        }
    }

    /**
     * Attempts to store the given players offhand or mainhand item in the toolbelt. Attempts to grab the offhand item
     * first and grabs the mainhand item if the offhand is empty.
     * @param player A player
     * @return false if the toolbelt is full, otherwise true
     */
    public static boolean storeItemInToolbelt(PlayerEntity player) {
        ItemStack toolbeltStack = findToolbelt(player);
        ItemStack itemStack = player.getHeldItem(Hand.OFF_HAND);
        Hand sourceHand = Hand.OFF_HAND;

        if (itemStack.isEmpty()) {
            itemStack = player.getHeldItem(Hand.MAIN_HAND);
            sourceHand = Hand.MAIN_HAND;
        }

        if (toolbeltStack.isEmpty() || itemStack.isEmpty() || itemStack.getItem() == ItemToolbeltModular.instance) {
            return true;
        }

        if (storeItemInToolbelt(toolbeltStack, itemStack)) {
            player.setHeldItem(sourceHand, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    public static boolean storeItemInToolbelt(ItemStack toolbeltStack, ItemStack itemStack) {
        if (new InventoryPotions(toolbeltStack).storeItemInInventory(itemStack)) {
            return true;
        }

        if (new InventoryQuiver(toolbeltStack).storeItemInInventory(itemStack)) {
            return true;
        }

        if (new InventoryQuickslot(toolbeltStack).storeItemInInventory(itemStack)) {
            return true;
        }

        if (new InventoryStorage(toolbeltStack).storeItemInInventory(itemStack)) {
            return true;
        }

        return false;
    }

    /**
     * Attempts to find the first itemstack containing a toolbelt in the given players inventory.
     * @param player A player
     * @return A toolbelt itemstack, or an empty itemstack if the player has no toolbelt
     */
    public static ItemStack findToolbelt(PlayerEntity player) {
        PlayerInventory inventoryPlayer = player.inventory;
        for (int i = 0; i < inventoryPlayer.mainInventory.size(); ++i) {
            ItemStack itemStack = inventoryPlayer.getStackInSlot(i);
            if (ItemToolbeltModular.instance.equals(itemStack.getItem())) {
                return itemStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void emptyOverflowSlots(ItemStack itemStack, PlayerEntity player) {
        new InventoryQuickslot(itemStack).emptyOverflowSlots(player);
        new InventoryPotions(itemStack).emptyOverflowSlots(player);
        new InventoryStorage(itemStack).emptyOverflowSlots(player);
        new InventoryQuiver(itemStack).emptyOverflowSlots(player);
    }

    /**
     * Attempts to find a suitable tool from the players quick access quickslots to be used on the given blockstate.
     * @param player The player
     * @param traceResult The raytrace result for where the cursor was when the event was triggered
     * @param blockState A blockstate
     * @return a quickslot inventory index if a suitable tool is found, otherwise -1
     */
    public static int getQuickAccessSlotIndex(PlayerEntity player, RayTraceResult traceResult, BlockState blockState) {
        ItemStack toolbeltStack = UtilToolbelt.findToolbelt(player);
        InventoryQuickslot inventory = new InventoryQuickslot(toolbeltStack);
        List<Collection<ItemEffect>> effects = inventory.getSlotEffects();

        if (traceResult instanceof BlockRayTraceResult) {
            BlockRayTraceResult trace = (BlockRayTraceResult) traceResult;
            Vec3d hitVector = trace.getHitVec();
            BlockPos blockPos = trace.getPos();

            // todo 1.14: renable when feature gen (and block interactions) is back
//            BlockInteraction blockInteraction = CastOptional.cast(blockState.getBlock(), IBlockCapabilityInteractive.class)
//                    .map(block -> BlockInteraction.getInteractionAtPoint(player, blockState, blockPos, trace.getFace(),
//                            (float) hitVector.x - blockPos.getX(),
//                            (float) hitVector.y - blockPos.getY(),
//                            (float) hitVector.z - blockPos.getZ()))
//                    .orElse(null);

            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack itemStack = inventory.getStackInSlot(i);
                if (effects.get(i).contains(ItemEffect.quickAccess) && !itemStack.isEmpty()) {
                    ToolType requiredTool = blockState.getHarvestTool();
                    if (requiredTool != null && itemStack.getItem().getHarvestLevel(itemStack, requiredTool, player, blockState) > -1) {
                        return i;
                    }

                    // todo 1.14: renable when feature gen (and block interactions) is back
//                    if (blockInteraction != null) {
//                        if (itemStack.getItem() instanceof ICapabilityProvider) {
//                            ICapabilityProvider providerItem = ((ICapabilityProvider) itemStack.getItem());
//                            if (providerItem.getCapabilityLevel(itemStack, blockInteraction.requiredCapability) >= blockInteraction.requiredLevel) {
//                                return i;
//                            }
//                        }
//                    }
                }
            }
        }

        return -1;
    }
}
