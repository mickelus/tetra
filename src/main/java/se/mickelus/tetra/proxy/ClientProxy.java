package se.mickelus.tetra.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.salvage.CapabililtyInteractiveOverlay;
import se.mickelus.tetra.blocks.workbench.WorkbenchTESR;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchScreen;
import se.mickelus.tetra.generation.ExtendedStructureTESR;
import se.mickelus.tetra.items.ITetraItem;

import java.util.Arrays;

public class ClientProxy implements IProxy {

    @Override
    public void preInit(ITetraItem[] items, ITetraBlock[] blocks) {
    }

    @Override
    public void init(FMLCommonSetupEvent event, ITetraItem[] items, ITetraBlock[] blocks) {
        Arrays.stream(items).forEach(ITetraItem::clientInit);
        Arrays.stream(blocks).forEach(ITetraBlock::clientInit);

        ClientRegistry.bindTileEntitySpecialRenderer(WorkbenchTile.class, new WorkbenchTESR());
        ScreenManager.registerFactory(WorkbenchTile.containerType, WorkbenchScreen::new);

        if (ConfigHandler.development.get()) {
            ClientRegistry.bindTileEntitySpecialRenderer(StructureBlockTileEntity.class, new ExtendedStructureTESR());
        }
    }

    @Override
    public void postInit() {
        MinecraftForge.EVENT_BUS.register(new CapabililtyInteractiveOverlay());
    }

    @Override
    public PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
