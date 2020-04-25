package se.mickelus.tetra.capabilities;

import com.google.common.collect.Sets;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.items.modular.ItemModular;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.QuickslotInventory;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.StorageInventory;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CapabilityHelper {

    public static int getItemCapabilityLevel(ItemStack itemStack, Capability capability) {
        return Optional.of(itemStack)
                .filter(stack -> !stack.isEmpty())
                .map(CapabilityHelper::getReplacement)
                .filter(stack -> stack.getItem() instanceof ICapabilityProvider)
                .map(stack -> ((ICapabilityProvider) stack.getItem()).getCapabilityLevel(stack, capability))
                .orElse(0);
    }

    public static Set<Capability> getItemCapabilities(ItemStack itemStack) {
        return Optional.of(itemStack)
                .filter(stack -> !stack.isEmpty())
                .map(CapabilityHelper::getReplacement)
                .filter(stack -> stack.getItem() instanceof ICapabilityProvider)
                .map(stack -> ((ICapabilityProvider) stack.getItem()).getCapabilities(stack))
                .orElse(Collections.emptySet());
    }

    public static int getPlayerEffectLevel(PlayerEntity player, ItemEffect effect) {
        return Stream.concat(player.inventory.offHandInventory.stream(), player.inventory.mainInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(CapabilityHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
                .map(itemStack -> ((ItemModular) itemStack.getItem()).getEffectLevel(itemStack, effect))
                .max(Integer::compare)
                .orElse(0);
    }

    public static double getPlayerEffectEfficiency(PlayerEntity player, ItemEffect effect) {
        return Stream.concat(player.inventory.offHandInventory.stream(), player.inventory.mainInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(CapabilityHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof ItemModular)
                .max(Comparator.comparingInt(itemStack -> ((ItemModular) itemStack.getItem()).getEffectLevel(itemStack, effect)))
                .map(itemStack -> ((ItemModular) itemStack.getItem()).getEffectEfficiency(itemStack, effect))
                .orElse(0d);
    }

    public static int getPlayerCapabilityLevel(PlayerEntity player, Capability capability) {
        return Stream.concat(player.inventory.offHandInventory.stream(), player.inventory.mainInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(CapabilityHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof ICapabilityProvider)
                .map(itemStack -> ((ICapabilityProvider) itemStack.getItem()).getCapabilityLevel(itemStack, capability))
                .max(Integer::compare)
                .orElse(0);
    }

    public static Set<Capability> getPlayerCapabilities(PlayerEntity player) {
        return Stream.concat(player.inventory.offHandInventory.stream(), player.inventory.mainInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(CapabilityHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof ICapabilityProvider)
                .flatMap(itemStack -> ((ICapabilityProvider) itemStack.getItem()).getCapabilities(itemStack).stream())
                .collect(Collectors.toSet());
    }

    public static int getInventoryCapabilityLevel(IInventory inventory, Capability capability) {
        int result = 0;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            int comparisonLevel = result;
            result = Optional.of(inventory.getStackInSlot(i))
                    .filter(itemStack -> !itemStack.isEmpty())
                    .map(CapabilityHelper::getReplacement)
                    .filter(itemStack -> itemStack.getItem() instanceof ICapabilityProvider)
                    .map(itemStack -> ((ICapabilityProvider) itemStack.getItem()).getCapabilityLevel(itemStack, capability))
                    .filter(level -> level > comparisonLevel)
                    .orElse(comparisonLevel);
        }
        return result;

    }

    public static Set<Capability> getInventoryCapabilities(IInventory inventory) {
        Set<Capability> result = new HashSet<>();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            Optional.of(inventory.getStackInSlot(i))
                    .filter(itemStack -> !itemStack.isEmpty())
                    .map(CapabilityHelper::getReplacement)
                    .filter(itemStack -> itemStack.getItem() instanceof ICapabilityProvider)
                    .map(itemStack -> ((ICapabilityProvider) itemStack.getItem()).getCapabilities(itemStack).stream())
                    .orElseGet(Stream::empty)
                    .forEach(result::add);

        }
        return result;
    }

    public static ItemStack getInventoryProvidingItemStack(IInventory inventory, Capability capability, int level) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack result = Optional.of(inventory.getStackInSlot(i))
                    .filter(itemStack -> !itemStack.isEmpty())
                    .map(CapabilityHelper::getReplacement)
                    .filter(itemStack -> itemStack.getItem() instanceof ICapabilityProvider)
                    .filter(itemStack -> ((ICapabilityProvider) itemStack.getItem()).getCapabilityLevel(itemStack, capability) >= level)
                    .orElse(ItemStack.EMPTY);

            if (!result.isEmpty()) {
                return result;
            }

        }
        return ItemStack.EMPTY;
    }

    public static ItemStack consumeCraftCapabilityInventory(IInventory inventory, PlayerEntity player, ItemStack targetStack,
            Capability capability, int level,  boolean consumeResources) {
        ItemStack itemStack = getInventoryProvidingItemStack(inventory, capability, level);
        if (itemStack.getItem() instanceof ItemModular) {
            return ((ItemModular) itemStack.getItem())
                    .onCraftConsumeCapability(itemStack, targetStack, player, capability, level, consumeResources);
        }

        return null;
    }

    public static ItemStack consumeActionCapabilityInventory(IInventory inventory, PlayerEntity player, ItemStack targetStack,
            Capability capability, int level,  boolean consumeResources) {
        ItemStack itemStack = getInventoryProvidingItemStack(inventory, capability, level);
        if (itemStack.getItem() instanceof ItemModular) {
            return ((ItemModular) itemStack.getItem())
                    .onActionConsumeCapability(itemStack, targetStack, player, capability, level, consumeResources);
        }

        return null;
    }

    private static ItemStack getReplacement(ItemStack itemStack) {
        ItemStack replacement = ItemUpgradeRegistry.instance.getReplacement(itemStack);
        if (!replacement.isEmpty()) {
            return replacement;
        }
        return itemStack;
    }

    public static int getBlockCapabilityLevel(World world, BlockPos pos, BlockState blockStateIn, Capability capability) {
        return Optional.of(blockStateIn)
                .filter(blockState -> blockState.getBlock() instanceof ITetraBlock)
                .map(blockState -> (ITetraBlock) blockState.getBlock())
                .map(block -> block.getCapabilityLevel(world, pos, blockStateIn, capability))
                .orElse(0);
    }

    public static Collection<Capability> getBlockCapabilities(World world, BlockPos pos, BlockState blockStateIn) {
        return Optional.of(blockStateIn)
                .filter(blockState -> blockState.getBlock() instanceof ITetraBlock)
                .map(blockState -> (ITetraBlock) blockState.getBlock())
                .map(block -> block.getCapabilities(world, pos, blockStateIn))
                .orElse(Collections.emptyList());
    }

    public static int getToolbeltCapabilityLevel(PlayerEntity player, Capability capability) {
        return Optional.of(ToolbeltHelper.findToolbelt(player))
                .filter(toolbeltStack -> !toolbeltStack.isEmpty())
                .map(toolbeltStack -> Math.max(
                        getInventoryCapabilityLevel(new QuickslotInventory(toolbeltStack), capability),
                        getInventoryCapabilityLevel(new StorageInventory(toolbeltStack), capability)))
                .orElse(0);
    }

    public static Set<Capability> getToolbeltCapabilities(PlayerEntity player) {
        return Optional.of(ToolbeltHelper.findToolbelt(player))
                .filter(toolbeltStack -> !toolbeltStack.isEmpty())
                .map(toolbeltStack -> (Set<Capability>) Sets.union(
                        getInventoryCapabilities(new QuickslotInventory(toolbeltStack)),
                        getInventoryCapabilities(new StorageInventory(toolbeltStack))))
                .orElse(Collections.emptySet());

    }

    @Nullable
    public static ItemStack consumeCraftCapabilityToolbelt(PlayerEntity player, ItemStack targetStack, Capability capability, int level,  boolean consumeResources) {
        return Optional.of(ToolbeltHelper.findToolbelt(player))
                .filter(toolbeltStack -> !toolbeltStack.isEmpty())
                .map(toolbeltStack -> {
                    QuickslotInventory quickslotInventory = new QuickslotInventory(toolbeltStack);
                    ItemStack result = consumeCraftCapabilityInventory(quickslotInventory, player, targetStack, capability, level, consumeResources);
                    if (result != null) {
                        quickslotInventory.markDirty();
                        return result;
                    }

                    StorageInventory storageInventory = new StorageInventory(toolbeltStack);
                    result = consumeCraftCapabilityInventory(quickslotInventory, player, targetStack, capability, level, consumeResources);
                    if (result != null) {
                        storageInventory.markDirty();
                        return result;
                    }

                    return null;
                })
                .orElse(null);

    }

    public static ItemStack consumeActionCapabilityToolbelt(PlayerEntity player, ItemStack targetStack, Capability capability, int level,  boolean consumeResources) {
        return Optional.of(ToolbeltHelper.findToolbelt(player))
                .filter(toolbeltStack -> !toolbeltStack.isEmpty())
                .map(toolbeltStack -> {
                    QuickslotInventory quickslotInventory = new QuickslotInventory(toolbeltStack);
                    ItemStack result = consumeActionCapabilityInventory(quickslotInventory, player, targetStack, capability, level, consumeResources);
                    if (result != null) {
                        quickslotInventory.markDirty();
                        return result;
                    }

                    StorageInventory storageInventory = new StorageInventory(toolbeltStack);
                    result = consumeActionCapabilityInventory(quickslotInventory, player, targetStack, capability, level, consumeResources);
                    if (result != null) {
                        storageInventory.markDirty();
                        return result;
                    }

                    return null;
                })
                .orElse(null);

    }

    public static ItemStack getToolbeltProvidingItemStack(Capability capability, int level, Entity entity) {
        return CastOptional.cast(entity, PlayerEntity.class)
                .map(ToolbeltHelper::findToolbelt)
                .map(toolbeltStack -> {
                    ItemStack itemStack = getInventoryProvidingItemStack(new QuickslotInventory(toolbeltStack), capability, level);

                    if (!itemStack.isEmpty()) {
                        return itemStack;
                    }

                    return getInventoryProvidingItemStack(new StorageInventory(toolbeltStack), capability, level);
                })
                .orElse(ItemStack.EMPTY);
    }

    public static int getCombinedCapabilityLevel(PlayerEntity player, World world, BlockPos pos, BlockState blockStateIn, Capability capability) {
        return IntStream.of(
                getPlayerCapabilityLevel(player, capability),
                getToolbeltCapabilityLevel(player, capability),
                getBlockCapabilityLevel(world, pos, blockStateIn, capability))
                .max()
                .orElse(0);
    }

    public static int[] getCombinedCapabilityLevels(PlayerEntity player, World world, BlockPos pos, BlockState blockStateIn) {
        return Arrays.stream(Capability.values())
                .mapToInt(capability -> getCombinedCapabilityLevel(player, world, pos, blockStateIn, capability))
                .toArray();
    }

    public static ItemStack getPlayerProvidingItemStack(Capability capability, int level, Entity entity) {
        return CastOptional.cast(entity, PlayerEntity.class)
                .map(player -> Stream.concat(Stream.of(player.getHeldItemMainhand(), player.getHeldItemOffhand()), player.inventory.mainInventory.stream()))
                .orElse(Stream.empty())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(CapabilityHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof ICapabilityProvider)
                .filter(itemStack -> ((ICapabilityProvider) itemStack.getItem()).getCapabilityLevel(itemStack, capability) >= level)
                .findAny()
                .orElse(ItemStack.EMPTY);
    }
}
