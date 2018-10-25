package se.mickelus.tetra.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import se.mickelus.tetra.network.PacketHandler;

public class TetraBlock extends Block implements ITetraBlock {

    protected boolean hasItem = false;

    public TetraBlock(Material material) {
        super(material);
    }

    @Override
    public void clientPreInit() {

    }

    @Override
    public void init(PacketHandler packetHandler) {

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
