package se.mickelus.tetra.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
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

public class TetraBlock extends Block implements ITetraBlock {

    protected boolean hasItem = false;

    public TetraBlock(Material material) {
        super(material);
    }

    @Override
    public boolean hasItem() {
        return hasItem;
    }

    @Override
    public void registerItem(IForgeRegistry<Item> registry) {
        Item item = new ItemBlock(this).setRegistryName(getRegistryName());
        registry.register(item);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
        }
    }
}
