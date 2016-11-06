package se.mickelus.tetra.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import se.mickelus.tetra.items.rocketBoots.OverlayRocketBoots;
import se.mickelus.tetra.items.toolbelt.ItemToolbelt;
import se.mickelus.tetra.items.toolbelt.OverlayToolbelt;

public class ClientProxy implements IProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        setModel(ItemToolbelt.instance);
    }

    private void setModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    @Override
    public void init(FMLInitializationEvent event) {

    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        System.out.println("============== POST INIT CLIENT PROXY");
        MinecraftForge.EVENT_BUS.register(new OverlayToolbelt(Minecraft.getMinecraft()));
        MinecraftForge.EVENT_BUS.register(new OverlayRocketBoots(Minecraft.getMinecraft()));
    }
}
