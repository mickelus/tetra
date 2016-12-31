package se.mickelus.tetra.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import se.mickelus.tetra.blocks.workbench.ITetraBlock;
import se.mickelus.tetra.items.ITetraItem;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.toolbelt.TickHandlerToolbelt;

public class ServerProxy implements IProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event, ITetraItem[] items, ITetraBlock[] blocks) {

    }

    @Override
    public void init(FMLInitializationEvent event) {

    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {

    }
}
