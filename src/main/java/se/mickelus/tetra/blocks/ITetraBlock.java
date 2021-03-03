package se.mickelus.tetra.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.IForgeRegistry;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.network.PacketHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ITetraBlock {

    @OnlyIn(Dist.CLIENT)
    default void clientInit() { }

    default void init(PacketHandler packetHandler) {}

    boolean hasItem();

    default void registerItem(IForgeRegistry<Item> registry) {
        if (this instanceof Block) {
            registerItem(registry, (Block) this);
        }
    }

    default void registerItem(IForgeRegistry<Item> registry, Block block) {
        Item item = new BlockItem(block, new Item.Properties().group(TetraItemGroup.instance))
                .setRegistryName(block.getRegistryName());

        registry.register(item);
    }

    default boolean canProvideTools(World world, BlockPos pos, BlockPos targetPos) {
        return false;
    }

    default Collection<ToolType> getTools(World world, BlockPos pos, BlockState blockState) {
        return Collections.emptyList();
    }

    default int getToolLevel(World world, BlockPos pos, BlockState blockState, ToolType toolType) {
        return -1;
    }

    default Map<ToolType, Integer> getToolLevels(World world, BlockPos pos, BlockState blockState) {
        return getTools(world, pos, blockState).stream()
                .collect(Collectors.toMap(Function.identity(), toolType -> getToolLevel(world, pos, blockState, toolType)));
    }

    default ItemStack onCraftConsumeTool(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, String slot, boolean isReplacing,
            PlayerEntity player, ToolType requiredTool, int requiredLevel, boolean consumeResources) {
        return null;
    }

    default ItemStack onActionConsumeTool(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player,
            ToolType requiredTool, int requiredLevel, boolean consumeResources) {
        return null;
    }

    default boolean canUnlockSchematics(World world, BlockPos pos, BlockPos targetPos) {
        return false;
    }

    default ResourceLocation[] getSchematics(World world, BlockPos pos, BlockState blockState) {
        return new ResourceLocation[0];
    }

    default boolean canUnlockCraftingEffects(World world, BlockPos pos, BlockPos targetPos) {
        return false;
    }

    default ResourceLocation[] getCraftingEffects(World world, BlockPos pos, BlockState blockState) {
        return new ResourceLocation[0];
    }
}
