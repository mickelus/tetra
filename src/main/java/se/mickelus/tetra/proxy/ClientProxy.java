package se.mickelus.tetra.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.salvage.InteractiveBlockOverlay;
import se.mickelus.tetra.blocks.workbench.WorkbenchTESR;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchScreen;
import se.mickelus.tetra.compat.botania.BotaniaCompat;
import se.mickelus.tetra.generation.ExtendedStructureTESR;
import se.mickelus.tetra.items.ITetraItem;
import se.mickelus.tetra.items.modular.ThrownModularItemEntity;
import se.mickelus.tetra.items.modular.ThrownModularItemRenderer;
import se.mickelus.tetra.properties.ReachEntityFix;

import java.util.Arrays;

public class ClientProxy implements IProxy {

    @Override
    public void preInit(ITetraItem[] items, ITetraBlock[] blocks) {
    }

    @Override
    public void init(FMLCommonSetupEvent event, ITetraItem[] items, ITetraBlock[] blocks) {
        Arrays.stream(items).forEach(ITetraItem::clientInit);
        Arrays.stream(blocks).forEach(ITetraBlock::clientInit);

        RenderingRegistry.registerEntityRenderingHandler(ThrownModularItemEntity.type, ThrownModularItemRenderer::new);

        // these are registered here as there are multiple instances of workbench blocks
        ClientRegistry.bindTileEntityRenderer(WorkbenchTile.type, WorkbenchTESR::new);
        ScreenManager.registerFactory(WorkbenchTile.containerType, WorkbenchScreen::new);

        if (ConfigHandler.development.get()) {
            ClientRegistry.bindTileEntityRenderer(TileEntityType.STRUCTURE_BLOCK, ExtendedStructureTESR::new);
        }

        BotaniaCompat.clientInit();

        MinecraftForge.EVENT_BUS.register(ReachEntityFix.class);
    }

    @Override
    public void postInit() {
        MinecraftForge.EVENT_BUS.register(new InteractiveBlockOverlay());
    }

    @Override
    public PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
