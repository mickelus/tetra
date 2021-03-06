package se.mickelus.tetra.items.modular.impl.toolbelt;

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
import org.apache.commons.lang3.tuple.ImmutableTriple;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.IntegrationHelper;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IBlockCapabilityInteractive;
import se.mickelus.tetra.capabilities.ICapabilityProvider;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.*;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.util.CastOptional;
import top.theillusivec4.curios.api.CuriosAPI;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ToolbeltHelper {
    public static void equipItemFromToolbelt(PlayerEntity player, ToolbeltSlotType slotType, int index, Hand hand) {
        ToolbeltInventory inventory = null;
        ItemStack toolbeltStack = findToolbelt(player);

        // stops things from crashing if the player has dropped the toolbelt stack after opening the overlay
        if (!(toolbeltStack.getItem() instanceof ModularToolbeltItem)) {
            return;
        }

        switch (slotType) {
            case quickslot:
                inventory = new QuickslotInventory(toolbeltStack);
                break;
            case potion:
                inventory = new PotionsInventory(toolbeltStack);
                break;
            case quiver:
                inventory = new QuiverInventory(toolbeltStack);
                break;
            case storage:
                inventory = new StorageInventory(toolbeltStack);
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
                    player.sendStatusMessage(new TranslationTextComponent("tetra.toolbelt.blocked"), true);
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

        if (toolbeltStack.isEmpty() || itemStack.isEmpty() || itemStack.getItem() == ModularToolbeltItem.instance) {
            return true;
        }

        if (storeItemInToolbelt(toolbeltStack, itemStack)) {
            player.setHeldItem(sourceHand, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    public static boolean storeItemInToolbelt(ItemStack toolbeltStack, ItemStack itemStack) {
        if (new PotionsInventory(toolbeltStack).storeItemInInventory(itemStack)) {
            return true;
        }

        if (new QuiverInventory(toolbeltStack).storeItemInInventory(itemStack)) {
            return true;
        }

        if (new QuickslotInventory(toolbeltStack).storeItemInInventory(itemStack)) {
            return true;
        }

        if (new StorageInventory(toolbeltStack).storeItemInInventory(itemStack)) {
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
        if (IntegrationHelper.isCuriosLoaded) {
            Optional<ImmutableTriple<String, Integer, ItemStack>> maybeToolbelt = CuriosAPI.getCurioEquipped(ModularToolbeltItem.instance, player);
            if (maybeToolbelt.isPresent()) {
                return maybeToolbelt.get().right;
            }
            if (ConfigHandler.toolbeltCurioOnly.get()) {
                return ItemStack.EMPTY;
            }
        }
        PlayerInventory inventoryPlayer = player.inventory;
        for (int i = 0; i < inventoryPlayer.mainInventory.size(); ++i) {
            ItemStack itemStack = inventoryPlayer.getStackInSlot(i);
            if (ModularToolbeltItem.instance.equals(itemStack.getItem())) {
                return itemStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void emptyOverflowSlots(ItemStack itemStack, PlayerEntity player) {
        new QuickslotInventory(itemStack).emptyOverflowSlots(player);
        new PotionsInventory(itemStack).emptyOverflowSlots(player);
        new StorageInventory(itemStack).emptyOverflowSlots(player);
        new QuiverInventory(itemStack).emptyOverflowSlots(player);
    }

    /**
     * Attempts to find a suitable tool from the players quick access quickslots to be used on the given blockstate.
     * @param player The player
     * @param traceResult The raytrace result for where the cursor was when the event was triggered
     * @param blockState A blockstate
     * @return a quickslot inventory index if a suitable tool is found, otherwise -1
     */
    public static int getQuickAccessSlotIndex(PlayerEntity player, RayTraceResult traceResult, BlockState blockState) {
        ItemStack toolbeltStack = ToolbeltHelper.findToolbelt(player);
        QuickslotInventory inventory = new QuickslotInventory(toolbeltStack);
        List<Collection<ItemEffect>> effects = inventory.getSlotEffects();

        if (traceResult instanceof BlockRayTraceResult) {
            BlockRayTraceResult trace = (BlockRayTraceResult) traceResult;
            Vec3d hitVector = trace.getHitVec();
            BlockPos blockPos = trace.getPos();

            BlockInteraction blockInteraction = CastOptional.cast(blockState.getBlock(), IBlockCapabilityInteractive.class)
                    .map(block -> BlockInteraction.getInteractionAtPoint(player, blockState, blockPos, trace.getFace(),
                            (float) hitVector.x - blockPos.getX(),
                            (float) hitVector.y - blockPos.getY(),
                            (float) hitVector.z - blockPos.getZ()))
                    .orElse(null);

            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack itemStack = inventory.getStackInSlot(i);
                if (effects.get(i).contains(ItemEffect.quickAccess) && !itemStack.isEmpty()) {
                    ToolType requiredTool = blockState.getHarvestTool();
                    if (requiredTool != null && itemStack.getItem().getHarvestLevel(itemStack, requiredTool, player, blockState) > -1) {
                        return i;
                    }

                    if (blockInteraction != null) {
                        if (itemStack.getItem() instanceof ICapabilityProvider) {
                            ICapabilityProvider providerItem = ((ICapabilityProvider) itemStack.getItem());
                            if (providerItem.getCapabilityLevel(itemStack, blockInteraction.requiredCapability) >= blockInteraction.requiredLevel) {
                                return i;
                            }
                        }
                    }
                }
            }
        }

        return -1;
    }
}
