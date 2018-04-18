package se.mickelus.tetra.proxy;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.workbench.TESRWorkbench;
import se.mickelus.tetra.blocks.workbench.TileEntityWorkbench;
import se.mickelus.tetra.client.model.ModularModelLoader;
import se.mickelus.tetra.items.ITetraItem;
import se.mickelus.tetra.items.rocketBoots.OverlayRocketBoots;
import se.mickelus.tetra.items.toolbelt.OverlayToolbelt;

import java.util.Arrays;

public class ClientProxy implements IProxy {

    static {
        ModelLoaderRegistry.registerLoader(ModularModelLoader.instance);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event, ITetraItem[] items, ITetraBlock[] blocks) {
        Arrays.stream(items).forEach(ITetraItem::clientPreInit);
        Arrays.stream(blocks).forEach(ITetraBlock::clientPreInit);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWorkbench.class, new TESRWorkbench());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new OverlayToolbelt(Minecraft.getMinecraft()));
        MinecraftForge.EVENT_BUS.register(new OverlayRocketBoots(Minecraft.getMinecraft()));
    }
}
