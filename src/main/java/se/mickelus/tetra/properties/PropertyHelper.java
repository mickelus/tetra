package se.mickelus.tetra.properties;

import com.google.common.collect.Sets;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.QuickslotInventory;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.StorageInventory;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.util.CastOptional;
import se.mickelus.tetra.util.InventoryStream;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PropertyHelper {

    public static int getItemToolLevel(ItemStack itemStack, ToolType tool) {
        return Optional.of(itemStack)
                .filter(stack -> !stack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(stack -> stack.getItem() instanceof IToolProvider)
                .map(stack -> ((IToolProvider) stack.getItem()).getToolLevel(stack, tool))
                .orElse(0);
    }

    public static Set<ToolType> getItemTools(ItemStack itemStack) {
        return Optional.of(itemStack)
                .filter(stack -> !stack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(stack -> stack.getItem() instanceof IToolProvider)
                .map(stack -> ((IToolProvider) stack.getItem()).getTools(stack))
                .orElse(Collections.emptySet());
    }

    public static int getPlayerEffectLevel(PlayerEntity player, ItemEffect effect) {
        return Stream.concat(player.inventory.offHandInventory.stream(), player.inventory.mainInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof IModularItem)
                .map(itemStack -> ((IModularItem) itemStack.getItem()).getEffectLevel(itemStack, effect))
                .max(Integer::compare)
                .orElse(0);
    }

    public static double getPlayerEffectEfficiency(PlayerEntity player, ItemEffect effect) {
        return Stream.concat(player.inventory.offHandInventory.stream(), player.inventory.mainInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof IModularItem)
                .max(Comparator.comparingInt(itemStack -> ((IModularItem) itemStack.getItem()).getEffectLevel(itemStack, effect)))
                .map(itemStack -> ((IModularItem) itemStack.getItem()).getEffectEfficiency(itemStack, effect))
                .orElse(0d);
    }

    public static int getPlayerToolLevel(PlayerEntity player, ToolType tool) {
        return Stream.concat(player.inventory.offHandInventory.stream(), player.inventory.mainInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                .map(itemStack -> ((IToolProvider) itemStack.getItem()).getToolLevel(itemStack, tool))
                .max(Integer::compare)
                .orElse(0);
    }

    public static Set<ToolType> getPlayerTools(PlayerEntity player) {
        return Stream.concat(player.inventory.offHandInventory.stream(), player.inventory.mainInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                .flatMap(itemStack -> ((IToolProvider) itemStack.getItem()).getTools(itemStack).stream())
                .collect(Collectors.toSet());
    }

    public static Map<ToolType, Integer> getPlayerToolLevels(PlayerEntity player) {
        return Stream.concat(player.inventory.offHandInventory.stream(), player.inventory.mainInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                .map(itemStack -> ((IToolProvider) itemStack.getItem()).getToolLevels(itemStack))
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Math::max));
    }

    public static int getInventoryToolLevel(IInventory inventory, ToolType tool) {
        int result = 0;
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            int comparisonLevel = result;
            result = Optional.of(inventory.getStackInSlot(i))
                    .filter(itemStack -> !itemStack.isEmpty())
                    .map(PropertyHelper::getReplacement)
                    .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                    .map(itemStack -> ((IToolProvider) itemStack.getItem()).getToolLevel(itemStack, tool))
                    .filter(level -> level > comparisonLevel)
                    .orElse(comparisonLevel);
        }
        return result;

    }

    public static Set<ToolType> getInventoryTools(IInventory inventory) {
        Set<ToolType> result = new HashSet<>();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            Optional.of(inventory.getStackInSlot(i))
                    .filter(itemStack -> !itemStack.isEmpty())
                    .map(PropertyHelper::getReplacement)
                    .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                    .map(itemStack -> ((IToolProvider) itemStack.getItem()).getTools(itemStack).stream())
                    .orElseGet(Stream::empty)
                    .forEach(result::add);

        }
        return result;
    }

    public static Map<ToolType, Integer> getInventoryToolLevels(IInventory inventory) {
        return InventoryStream.of(inventory)
                .filter(itemStack -> !itemStack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                .map(itemStack -> ((IToolProvider) itemStack.getItem()).getToolLevels(itemStack))
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Math::max));
    }

    public static ItemStack getInventoryProvidingItemStack(IInventory inventory, ToolType tool, int level) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack result = Optional.of(inventory.getStackInSlot(i))
                    .filter(itemStack -> !itemStack.isEmpty())
                    .map(PropertyHelper::getReplacement)
                    .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                    .filter(itemStack -> ((IToolProvider) itemStack.getItem()).getToolLevel(itemStack, tool) >= level)
                    .orElse(ItemStack.EMPTY);

            if (!result.isEmpty()) {
                return result;
            }

        }
        return ItemStack.EMPTY;
    }

    public static ItemStack getPlayerProvidingItemStack(ToolType tool, int level, Entity entity) {
        return CastOptional.cast(entity, PlayerEntity.class)
                .map(player -> Stream.concat(Stream.of(player.getHeldItemMainhand(), player.getHeldItemOffhand()), player.inventory.mainInventory.stream()))
                .orElse(Stream.empty())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                .filter(itemStack -> ((IToolProvider) itemStack.getItem()).getToolLevel(itemStack, tool) >= level)
                .findAny()
                .orElse(ItemStack.EMPTY);
    }

    public static ItemStack consumeCraftToolInventory(IInventory inventory, PlayerEntity player, ItemStack targetStack,
            ToolType tool, int level,  boolean consumeResources) {
        ItemStack itemStack = getInventoryProvidingItemStack(inventory, tool, level);
        if (itemStack.getItem() instanceof IToolProvider) {
            return ((IToolProvider) itemStack.getItem())
                    .onCraftConsume(itemStack, targetStack, player, tool, level, consumeResources);
        }

        return null;
    }

    public static ItemStack consumeActionToolInventory(IInventory inventory, PlayerEntity player, ItemStack targetStack,
            ToolType tool, int level,  boolean consumeResources) {
        ItemStack itemStack = getInventoryProvidingItemStack(inventory, tool, level);
        if (itemStack.getItem() instanceof IToolProvider) {
            return ((IToolProvider) itemStack.getItem())
                    .onActionConsume(itemStack, targetStack, player, tool, level, consumeResources);
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

    public static int getBlockToolLevel(World world, BlockPos pos, BlockState blockStateIn, ToolType tool) {
        return Optional.of(blockStateIn)
                .filter(blockState -> blockState.getBlock() instanceof ITetraBlock)
                .map(blockState -> (ITetraBlock) blockState.getBlock())
                .map(block -> block.getToolLevel(world, pos, blockStateIn, tool))
                .orElse(0);
    }

    public static Collection<ToolType> getBlockTools(World world, BlockPos pos, BlockState blockStateIn) {
        return Optional.of(blockStateIn)
                .filter(blockState -> blockState.getBlock() instanceof ITetraBlock)
                .map(blockState -> (ITetraBlock) blockState.getBlock())
                .map(block -> block.getTools(world, pos, blockStateIn))
                .orElse(Collections.emptyList());
    }

    public static Map<ToolType, Integer> getBlockToolLevels(World world, BlockPos pos, BlockState blockStateIn) {
        return Optional.of(blockStateIn)
                .filter(blockState -> blockState.getBlock() instanceof ITetraBlock)
                .map(blockState -> (ITetraBlock) blockState.getBlock())
                .map(block -> block.getToolLevels(world, pos, blockStateIn))
                .orElse(Collections.emptyMap());
    }

    public static int getToolbeltToolLevel(PlayerEntity player, ToolType tool) {
        return Optional.of(ToolbeltHelper.findToolbelt(player))
                .filter(toolbeltStack -> !toolbeltStack.isEmpty())
                .map(toolbeltStack -> Math.max(
                        getInventoryToolLevel(new QuickslotInventory(toolbeltStack), tool),
                        getInventoryToolLevel(new StorageInventory(toolbeltStack), tool)))
                .orElse(0);
    }

    public static Set<ToolType> getToolbeltTools(PlayerEntity player) {
        return Optional.of(ToolbeltHelper.findToolbelt(player))
                .filter(toolbeltStack -> !toolbeltStack.isEmpty())
                .map(toolbeltStack -> (Set<ToolType>) Sets.union(
                        getInventoryTools(new QuickslotInventory(toolbeltStack)),
                        getInventoryTools(new StorageInventory(toolbeltStack))))
                .orElse(Collections.emptySet());

    }

    public static Map<ToolType, Integer> getToolbeltToolLevels(PlayerEntity player) {
        return Optional.of(ToolbeltHelper.findToolbelt(player))
                .filter(toolbeltStack -> !toolbeltStack.isEmpty())
                .map(toolbeltStack -> Stream.of(
                        getInventoryToolLevels(new QuickslotInventory(toolbeltStack)),
                        getInventoryToolLevels(new StorageInventory(toolbeltStack))))
                .orElseGet(Stream::empty)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Math::max));

    }

    @Nullable
    public static ItemStack consumeCraftToolToolbelt(PlayerEntity player, ItemStack targetStack, ToolType tool, int level,  boolean consumeResources) {
        return Optional.of(ToolbeltHelper.findToolbelt(player))
                .filter(toolbeltStack -> !toolbeltStack.isEmpty())
                .map(toolbeltStack -> {
                    QuickslotInventory quickslotInventory = new QuickslotInventory(toolbeltStack);
                    ItemStack result = consumeCraftToolInventory(quickslotInventory, player, targetStack, tool, level, consumeResources);
                    if (result != null) {
                        quickslotInventory.markDirty();
                        return result;
                    }

                    StorageInventory storageInventory = new StorageInventory(toolbeltStack);
                    result = consumeCraftToolInventory(quickslotInventory, player, targetStack, tool, level, consumeResources);
                    if (result != null) {
                        storageInventory.markDirty();
                        return result;
                    }

                    return null;
                })
                .orElse(null);

    }

    public static ItemStack consumeActionToolToolbelt(PlayerEntity player, ItemStack targetStack, ToolType tool, int level,  boolean consumeResources) {
        return Optional.of(ToolbeltHelper.findToolbelt(player))
                .filter(toolbeltStack -> !toolbeltStack.isEmpty())
                .map(toolbeltStack -> {
                    QuickslotInventory quickslotInventory = new QuickslotInventory(toolbeltStack);
                    ItemStack result = consumeActionToolInventory(quickslotInventory, player, targetStack, tool, level, consumeResources);
                    if (result != null) {
                        quickslotInventory.markDirty();
                        return result;
                    }

                    StorageInventory storageInventory = new StorageInventory(toolbeltStack);
                    result = consumeActionToolInventory(quickslotInventory, player, targetStack, tool, level, consumeResources);
                    if (result != null) {
                        storageInventory.markDirty();
                        return result;
                    }

                    return null;
                })
                .orElse(null);

    }

    public static ItemStack getToolbeltProvidingItemStack(ToolType tool, int level, PlayerEntity player) {
        return Optional.of(ToolbeltHelper.findToolbelt(player))
                .filter(itemStack -> !itemStack.isEmpty())
                .map(toolbeltStack -> {
                    ItemStack itemStack = getInventoryProvidingItemStack(new QuickslotInventory(toolbeltStack), tool, level);

                    if (!itemStack.isEmpty()) {
                        return itemStack;
                    }

                    return getInventoryProvidingItemStack(new StorageInventory(toolbeltStack), tool, level);
                })
                .orElse(ItemStack.EMPTY);
    }

    public static int getCombinedToolLevel(PlayerEntity player, World world, BlockPos pos, BlockState blockStateIn, ToolType tool) {
        return IntStream.of(
                getPlayerToolLevel(player, tool),
                getToolbeltToolLevel(player, tool),
                getBlockToolLevel(world, pos, blockStateIn, tool))
                .max()
                .orElse(0);
    }

    public static Map<ToolType, Integer> getCombinedToolLevels(PlayerEntity player, World world, BlockPos pos, BlockState blockStateIn) {
        return Stream.of(
                getInventoryToolLevels(player.inventory),
                getToolbeltToolLevels(player),
                getBlockToolLevels(world, pos, blockStateIn))
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Math::max));
    }
}
