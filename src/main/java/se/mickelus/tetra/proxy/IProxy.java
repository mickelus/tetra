package se.mickelus.tetra.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import se.mickelus.tetra.blocks.workbench.ITetraBlock;
import se.mickelus.tetra.items.ITetraItem;
import se.mickelus.tetra.items.TetraItem;

public interface IProxy {

    public void preInit(FMLPreInitializationEvent event, ITetraItem[] items, ITetraBlock[] blocks);
    public void init(FMLInitializationEvent event);
    public void postInit(FMLPostInitializationEvent event);
}
