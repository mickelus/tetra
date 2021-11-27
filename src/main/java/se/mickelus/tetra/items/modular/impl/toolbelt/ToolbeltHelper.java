package se.mickelus.tetra.items.modular.impl.toolbelt;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ToolType;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.blocks.salvage.BlockInteraction;
import se.mickelus.tetra.blocks.salvage.IInteractiveBlock;
import se.mickelus.tetra.compat.curios.CuriosCompat;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.properties.IToolProvider;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.*;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.util.CastOptional;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.*;

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
        if (CuriosCompat.isLoaded) {
            Optional<ImmutableTriple<String, Integer, ItemStack>> maybeToolbelt = CuriosApi.getCuriosHelper().findEquippedCurio(ModularToolbeltItem.instance, player);
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

    public static List<ItemStack> getToolbeltItems(PlayerEntity player) {
        return Optional.of(ToolbeltHelper.findToolbelt(player))
                .filter(toolbeltStack -> !toolbeltStack.isEmpty())
                .map(toolbeltStack -> {
                    QuickslotInventory quickslots = new QuickslotInventory(toolbeltStack);
                    StorageInventory storage = new StorageInventory(toolbeltStack);
                    List<ItemStack> result = new ArrayList<>(quickslots.getSizeInventory() + storage.getSizeInventory());

                    for (int i = 0; i < quickslots.getSizeInventory(); i++) {
                        result.add(i, quickslots.getStackInSlot(i));
                    }

                    for (int i = 0; i < storage.getSizeInventory(); i++) {
                        result.add(quickslots.getSizeInventory() + i, storage.getStackInSlot(i));
                    }

                    return result;
                })
                .orElse(Collections.emptyList());
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

        if (toolbeltStack.isEmpty()) {
            return -1;
        }

        QuickslotInventory inventory = new QuickslotInventory(toolbeltStack);
        List<Collection<ItemEffect>> effects = inventory.getSlotEffects();

        if (traceResult instanceof BlockRayTraceResult) {
            BlockRayTraceResult trace = (BlockRayTraceResult) traceResult;
            Vector3d hitVector = trace.getHitVec();
            BlockPos blockPos = trace.getPos();

            BlockInteraction blockInteraction = CastOptional.cast(blockState.getBlock(), IInteractiveBlock.class)
                    .map(block -> BlockInteraction.getInteractionAtPoint(player, blockState, blockPos, trace.getFace(),
                            (float) hitVector.x - blockPos.getX(),
                            (float) hitVector.y - blockPos.getY(),
                            (float) hitVector.z - blockPos.getZ()))
                    .orElse(null);

            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack itemStack = inventory.getStackInSlot(i);
                if (effects.get(i).contains(ItemEffect.quickAccess) && !itemStack.isEmpty()) {
                    ToolType requiredTool = blockState.getHarvestTool();
                    ToolType effectiveTool = ItemModularHandheld.getEffectiveTool(blockState);
                    if (requiredTool != null
                            && itemStack.getItem().getHarvestLevel(itemStack, requiredTool, player, blockState) >= blockState.getHarvestLevel()
                            || effectiveTool != null && itemStack.getItem().getToolTypes(itemStack).contains(effectiveTool)) {
                        return i;
                    }

                    if (ItemModularHandheld.canDenail(blockState)) {
                        boolean itemCanDenail = CastOptional.cast(itemStack.getItem(), IModularItem.class)
                                .map(item -> item.getEffectLevel(itemStack, ItemEffect.denailing) > 0)
                                .orElse(false);
                        if (itemCanDenail) {
                            return i;
                        }
                    }


                    if (blockInteraction != null) {
                        if (itemStack.getItem() instanceof IToolProvider) {
                            IToolProvider providerItem = ((IToolProvider) itemStack.getItem());
                            if (providerItem.getToolLevel(itemStack, blockInteraction.requiredTool) >= blockInteraction.requiredLevel) {
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
