package se.mickelus.tetra.properties;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.mutil.util.InventoryStream;
import se.mickelus.tetra.blocks.IToolProviderBlock;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.toolbelt.ToolbeltHelper;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.QuickslotInventory;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.StorageInventory;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class PropertyHelper {

    public static int getItemToolLevel(ItemStack itemStack, ToolAction tool) {
        return Optional.of(itemStack)
                .filter(stack -> !stack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(stack -> stack.getItem() instanceof IToolProvider)
                .map(stack -> ((IToolProvider) stack.getItem()).getToolLevel(stack, tool))
                .orElse(0);
    }

    public static Set<ToolAction> getItemTools(ItemStack itemStack) {
        return Optional.of(itemStack)
                .filter(stack -> !stack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(stack -> stack.getItem() instanceof IToolProvider)
                .map(stack -> ((IToolProvider) stack.getItem()).getTools(stack))
                .orElse(Collections.emptySet());
    }

    public static int getPlayerEffectLevel(Player player, ItemEffect effect) {
        return Stream.concat(player.getInventory().offhand.stream(), player.getInventory().items.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof IModularItem)
                .map(itemStack -> ((IModularItem) itemStack.getItem()).getEffectLevel(itemStack, effect))
                .max(Integer::compare)
                .orElse(0);
    }

    public static double getPlayerEffectEfficiency(Player player, ItemEffect effect) {
        return Stream.concat(player.getInventory().offhand.stream(), player.getInventory().items.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof IModularItem)
                .max(Comparator.comparingInt(itemStack -> ((IModularItem) itemStack.getItem()).getEffectLevel(itemStack, effect)))
                .map(itemStack -> ((IModularItem) itemStack.getItem()).getEffectEfficiency(itemStack, effect))
                .orElse(0d);
    }

    public static int getPlayerToolLevel(Player player, ToolAction tool) {
        return Stream.concat(player.getInventory().offhand.stream(), player.getInventory().items.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                .map(itemStack -> ((IToolProvider) itemStack.getItem()).getToolLevel(itemStack, tool))
                .max(Integer::compare)
                .orElse(0);
    }

    public static Set<ToolAction> getPlayerTools(Player player) {
        return Stream.concat(player.getInventory().offhand.stream(), player.getInventory().items.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                .flatMap(itemStack -> ((IToolProvider) itemStack.getItem()).getTools(itemStack).stream())
                .collect(Collectors.toSet());
    }

    public static Map<ToolAction, Integer> getPlayerToolLevels(Player player) {
        return Stream.concat(player.getInventory().offhand.stream(), player.getInventory().items.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                .map(itemStack -> ((IToolProvider) itemStack.getItem()).getToolLevels(itemStack))
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Math::max));
    }

    public static int getInventoryToolLevel(Container inventory, ToolAction tool) {
        int result = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            int comparisonLevel = result;
            result = Optional.of(inventory.getItem(i))
                    .filter(itemStack -> !itemStack.isEmpty())
                    .map(PropertyHelper::getReplacement)
                    .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                    .map(itemStack -> ((IToolProvider) itemStack.getItem()).getToolLevel(itemStack, tool))
                    .filter(level -> level > comparisonLevel)
                    .orElse(comparisonLevel);
        }
        return result;

    }

    public static Set<ToolAction> getInventoryTools(Container inventory) {
        Set<ToolAction> result = new HashSet<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            Optional.of(inventory.getItem(i))
                    .filter(itemStack -> !itemStack.isEmpty())
                    .map(PropertyHelper::getReplacement)
                    .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                    .map(itemStack -> ((IToolProvider) itemStack.getItem()).getTools(itemStack).stream())
                    .orElseGet(Stream::empty)
                    .forEach(result::add);

        }
        return result;
    }

    public static Map<ToolAction, Integer> getInventoryToolLevels(Container inventory) {
        return InventoryStream.of(inventory)
                .filter(itemStack -> !itemStack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                .map(itemStack -> ((IToolProvider) itemStack.getItem()).getToolLevels(itemStack))
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Math::max));
    }

    public static ItemStack getInventoryProvidingItemStack(Container inventory, ToolAction tool, int level) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack result = Optional.of(inventory.getItem(i))
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

    public static ItemStack getPlayerProvidingItemStack(ToolAction tool, int level, Entity entity) {
        return CastOptional.cast(entity, Player.class)
                .map(player -> Stream.concat(Stream.of(player.getMainHandItem(), player.getOffhandItem()), player.getInventory().items.stream()))
                .orElse(Stream.empty())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(PropertyHelper::getReplacement)
                .filter(itemStack -> itemStack.getItem() instanceof IToolProvider)
                .filter(itemStack -> ((IToolProvider) itemStack.getItem()).getToolLevel(itemStack, tool) >= level)
                .findAny()
                .orElse(ItemStack.EMPTY);
    }

    public static ItemStack consumeCraftToolInventory(Container inventory, Player player, ItemStack targetStack,
            ToolAction tool, int level, boolean consumeResources) {
        ItemStack itemStack = getInventoryProvidingItemStack(inventory, tool, level);
        if (itemStack.getItem() instanceof IToolProvider) {
            return ((IToolProvider) itemStack.getItem())
                    .onCraftConsume(itemStack, targetStack, player, tool, level, consumeResources);
        }

        return null;
    }

    public static ItemStack consumeActionToolInventory(Container inventory, Player player, ItemStack targetStack,
            ToolAction tool, int level, boolean consumeResources) {
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

    public static int getBlockToolLevel(Level world, BlockPos pos, BlockState blockStateIn, ToolAction tool) {
        return Optional.of(blockStateIn)
                .filter(blockState -> blockState.getBlock() instanceof IToolProviderBlock)
                .map(blockState -> (IToolProviderBlock) blockState.getBlock())
                .map(block -> block.getToolLevel(world, pos, blockStateIn, tool))
                .orElse(0);
    }

    public static Collection<ToolAction> getBlockTools(Level world, BlockPos pos, BlockState blockStateIn) {
        return Optional.of(blockStateIn)
                .filter(blockState -> blockState.getBlock() instanceof IToolProviderBlock)
                .map(blockState -> (IToolProviderBlock) blockState.getBlock())
                .map(block -> block.getTools(world, pos, blockStateIn))
                .orElse(Collections.emptyList());
    }

    public static Map<ToolAction, Integer> getBlockToolLevels(Level world, BlockPos pos, BlockState blockStateIn) {
        return Optional.of(blockStateIn)
                .filter(blockState -> blockState.getBlock() instanceof IToolProviderBlock)
                .map(blockState -> (IToolProviderBlock) blockState.getBlock())
                .map(block -> block.getToolLevels(world, pos, blockStateIn))
                .orElse(Collections.emptyMap());
    }

    public static int getToolbeltToolLevel(Player player, ToolAction tool) {
        return Optional.of(ToolbeltHelper.findToolbelt(player))
                .filter(toolbeltStack -> !toolbeltStack.isEmpty())
                .map(toolbeltStack -> Math.max(
                        getInventoryToolLevel(new QuickslotInventory(toolbeltStack), tool),
                        getInventoryToolLevel(new StorageInventory(toolbeltStack), tool)))
                .orElse(0);
    }

    public static Set<ToolAction> getToolbeltTools(Player player) {
        return Optional.of(ToolbeltHelper.findToolbelt(player))
                .filter(toolbeltStack -> !toolbeltStack.isEmpty())
                .map(toolbeltStack -> (Set<ToolAction>) Sets.union(
                        getInventoryTools(new QuickslotInventory(toolbeltStack)),
                        getInventoryTools(new StorageInventory(toolbeltStack))))
                .orElse(Collections.emptySet());

    }

    public static Map<ToolAction, Integer> getToolbeltToolLevels(Player player) {
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
    public static ItemStack consumeCraftToolToolbelt(Player player, ItemStack targetStack, ToolAction tool, int level, boolean consumeResources) {
        return Optional.of(ToolbeltHelper.findToolbelt(player))
                .filter(toolbeltStack -> !toolbeltStack.isEmpty())
                .map(toolbeltStack -> {
                    QuickslotInventory quickslotInventory = new QuickslotInventory(toolbeltStack);
                    ItemStack result = consumeCraftToolInventory(quickslotInventory, player, targetStack, tool, level, consumeResources);
                    if (result != null) {
                        quickslotInventory.setChanged();
                        return result;
                    }

                    StorageInventory storageInventory = new StorageInventory(toolbeltStack);
                    result = consumeCraftToolInventory(quickslotInventory, player, targetStack, tool, level, consumeResources);
                    if (result != null) {
                        storageInventory.setChanged();
                        return result;
                    }

                    return null;
                })
                .orElse(null);

    }

    public static ItemStack consumeActionToolToolbelt(Player player, ItemStack targetStack, ToolAction tool, int level, boolean consumeResources) {
        return Optional.of(ToolbeltHelper.findToolbelt(player))
                .filter(toolbeltStack -> !toolbeltStack.isEmpty())
                .map(toolbeltStack -> {
                    QuickslotInventory quickslotInventory = new QuickslotInventory(toolbeltStack);
                    ItemStack result = consumeActionToolInventory(quickslotInventory, player, targetStack, tool, level, consumeResources);
                    if (result != null) {
                        quickslotInventory.setChanged();
                        return result;
                    }

                    StorageInventory storageInventory = new StorageInventory(toolbeltStack);
                    result = consumeActionToolInventory(quickslotInventory, player, targetStack, tool, level, consumeResources);
                    if (result != null) {
                        storageInventory.setChanged();
                        return result;
                    }

                    return null;
                })
                .orElse(null);

    }

    public static ItemStack getToolbeltProvidingItemStack(ToolAction tool, int level, Player player) {
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

    public static int getCombinedToolLevel(Player player, Level world, BlockPos pos, BlockState blockStateIn, ToolAction tool) {
        return IntStream.of(
                        getPlayerToolLevel(player, tool),
                        getToolbeltToolLevel(player, tool),
                        getBlockToolLevel(world, pos, blockStateIn, tool))
                .max()
                .orElse(0);
    }

    public static Map<ToolAction, Integer> getCombinedToolLevels(Player player, Level world, BlockPos pos, BlockState blockStateIn) {
        return Stream.of(
                        getInventoryToolLevels(player.getInventory()),
                        getToolbeltToolLevels(player),
                        getBlockToolLevels(world, pos, blockStateIn))
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Math::max));
    }
}
