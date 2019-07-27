package se.mickelus.tetra.proxy;

import com.google.common.collect.Maps;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.animation.AnimationTESR;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.forged.BlockForgedCrate;
import se.mickelus.tetra.blocks.forged.container.BlockForgedContainer;
import se.mickelus.tetra.blocks.forged.container.TESRForgedContainer;
import se.mickelus.tetra.blocks.forged.container.TileEntityForgedContainer;
import se.mickelus.tetra.blocks.forged.extractor.BlockSeepingBedrock;
import se.mickelus.tetra.blocks.forged.extractor.TileEntityCoreExtractorPiston;
import se.mickelus.tetra.blocks.hammer.TileEntityHammerHead;
import se.mickelus.tetra.blocks.salvage.CapabililtyInteractiveOverlay;
import se.mickelus.tetra.blocks.workbench.TESRWorkbench;
import se.mickelus.tetra.blocks.workbench.TileEntityWorkbench;
import se.mickelus.tetra.client.model.ModularModelLoader;
import se.mickelus.tetra.generation.ExtendedStructureTESR;
import se.mickelus.tetra.items.ITetraItem;
import se.mickelus.tetra.items.toolbelt.booster.OverlayBooster;
import se.mickelus.tetra.items.toolbelt.OverlayToolbelt;

import java.util.Arrays;
import java.util.Map;

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
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHammerHead.class, new AnimationTESR<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCoreExtractorPiston.class, new AnimationTESR<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityForgedContainer.class, new TESRForgedContainer());

        if (ConfigHandler.development) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStructure.class, new ExtendedStructureTESR());
        }
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new OverlayToolbelt(Minecraft.getMinecraft()));
        MinecraftForge.EVENT_BUS.register(new OverlayBooster(Minecraft.getMinecraft()));
        MinecraftForge.EVENT_BUS.register(new CapabililtyInteractiveOverlay());
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {


        if (ConfigHandler.generate_features) {
            // provides a decent item model for the container (which uses a TESR) without messing around with millions of blockstate variants
            ModelLoader.setCustomStateMapper(BlockForgedContainer.instance, new StateMapperBase() {
                @Override
                protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                    return new ModelResourceLocation(TetraMod.MOD_ID + ":forged_container");
                }
            });

            ModelLoader.setCustomStateMapper(BlockForgedCrate.instance, new StateMap.Builder().ignore(BlockForgedCrate.propIntegrity).build());

            ModelLoader.setCustomStateMapper(BlockSeepingBedrock.instance, new StateMapperBase() {
                @Override
                protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                    return new ModelResourceLocation(TetraMod.MOD_ID + ":seeping_bedrock",
                            "active=" + (state.getValue(BlockSeepingBedrock.propActive) > 0));
                }
            });
        }
    }
}
