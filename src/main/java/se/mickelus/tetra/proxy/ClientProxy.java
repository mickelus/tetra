package se.mickelus.tetra.proxy;

import net.minecraft.client.Minecraft;
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

        // todo 1.14: readd when terrain gen is back
//        ClientRegistry.bindTileEntitySpecialRenderer(HammerHeadTile.class, new TileEntityRendererAnimation<>());
//        ClientRegistry.bindTileEntitySpecialRenderer(CoreExtractorPistonTile.class, new TileEntityRendererAnimation<>());
//        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityForgedContainer.class, new TESRForgedContainer());

        if (ConfigHandler.development.get()) {
            ClientRegistry.bindTileEntitySpecialRenderer(StructureBlockTileEntity.class, new ExtendedStructureTESR());
        }
    }

    @Override
    public void postInit() {
        MinecraftForge.EVENT_BUS.register(new CapabililtyInteractiveOverlay());
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        if (ConfigHandler.generateFeatures.get()) {
            // provides a decent item model for the container (which uses a TESR) without messing around with millions of blockstate variants

            // todo 1.14: statemappers should no longer be required as props can be ignored in the blockstate .json instead
//            ModelLoader.setCustomStateMapper(BlockForgedContainer.instance, new StateMapperBase() {
//                @Override
//                protected ModelResourceLocation getModelResourceLocation(BlockState state) {
//                    return new ModelResourceLocation(TetraMod.MOD_ID + ":forged_container");
//                }
//            });
//
//            ModelLoader.setCustomStateMapper(BlockForgedCrate.instance, new StateMap.Builder().ignore(BlockForgedCrate.propIntegrity).build());
        }
    }

    @Override
    public PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
