package se.mickelus.tetra.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.network.PacketHandler;

import java.util.Collection;
import java.util.Collections;

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

    default boolean canProvideCapabilities(World world, BlockPos pos, BlockPos targetPos) {
        return false;
    }

    default Collection<Capability> getCapabilities(World world, BlockPos pos, BlockState blockState) {
        return Collections.emptyList();
    }

    default int getCapabilityLevel(World world, BlockPos pos, BlockState blockState, Capability capability) {
        return -1;
    }

    default ItemStack onCraftConsumeCapability(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player,
            Capability requiredCapability, int requiredLevel, boolean consumeResources) {
        return null;
    }

    default ItemStack onActionConsumeCapability(World world, BlockPos pos, BlockState blockState, ItemStack targetStack, PlayerEntity player,
            Capability requiredCapability, int requiredLevel, boolean consumeResources) {
        return null;
    }
}
