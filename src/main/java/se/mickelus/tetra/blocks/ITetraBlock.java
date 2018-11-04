package se.mickelus.tetra.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.network.PacketHandler;

import java.util.Collection;

public interface ITetraBlock {
    public void clientPreInit();
    public void init(PacketHandler packetHandler);

    public boolean hasItem();
    public void registerItem(IForgeRegistry<Item> registry);
    
    public Collection<Capability> getCapabilities(World world, BlockPos pos, IBlockState blockState);
    public int getCapabilityLevel(World world, BlockPos pos, IBlockState blockState, Capability capability);
    public ItemStack onCraftConsumeCapability(World world, BlockPos pos, IBlockState blockState, ItemStack targetStack, EntityPlayer player, boolean consumeResources);
}
