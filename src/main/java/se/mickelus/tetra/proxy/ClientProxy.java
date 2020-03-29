package se.mickelus.tetra.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.entity.TridentRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.salvage.CapabililtyInteractiveOverlay;
import se.mickelus.tetra.blocks.workbench.WorkbenchTESR;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchScreen;
import se.mickelus.tetra.client.model.ModularModelLoader;
import se.mickelus.tetra.generation.ExtendedStructureTESR;
import se.mickelus.tetra.items.ITetraItem;
import se.mickelus.tetra.items.modular.ThrownModularItemEntity;
import se.mickelus.tetra.items.modular.ThrownModularItemRenderer;

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

        ClientRegistry.bindTileEntityRenderer(WorkbenchTile.type, WorkbenchTESR::new);
        ScreenManager.registerFactory(WorkbenchTile.containerType, WorkbenchScreen::new);

        if (ConfigHandler.development.get()) {
            ClientRegistry.bindTileEntityRenderer(TileEntityType.STRUCTURE_BLOCK, ExtendedStructureTESR::new);
        }

        ModelLoaderRegistry.registerLoader(new ResourceLocation(TetraMod.MOD_ID, "modular_loader"), new ModularModelLoader());
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
