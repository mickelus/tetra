package se.mickelus.tetra.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.network.PacketHandler;

import java.util.Collection;
import java.util.Collections;

public interface ITetraBlock {

    default void clientPreInit() { }

    default void init(PacketHandler packetHandler) {}

    boolean hasItem();

    void registerItem(IForgeRegistry<Item> registry);

    default void registerItem(IForgeRegistry<Item> registry, Block block) {
        Item item = new ItemBlock(block).setRegistryName(block.getRegistryName());
        registry.register(item);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
        }
    }

    default Collection<Capability> getCapabilities(World world, BlockPos pos, IBlockState blockState) {
        return Collections.emptyList();
    }

    default int getCapabilityLevel(World world, BlockPos pos, IBlockState blockState, Capability capability) {
        return -1;
    }


    default ItemStack onCraftConsumeCapability(World world, BlockPos pos, IBlockState blockState, ItemStack targetStack, PlayerEntity player, boolean consumeResources) {
        return targetStack;
    }

    default ItemStack onActionConsumeCapability(World world, BlockPos pos, IBlockState blockState, ItemStack targetStack, PlayerEntity player, boolean consumeResources) {
        return targetStack;
    }
}
