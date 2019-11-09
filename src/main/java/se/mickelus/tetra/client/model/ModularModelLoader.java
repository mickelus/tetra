package se.mickelus.tetra.client.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.item.Item;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import se.mickelus.tetra.TetraLogger;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.sword.ItemSwordModular;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class ModularModelLoader {

    private static final Logger logger = LogManager.getLogger();

    private static List<ItemModular> items = new ArrayList<>();

    public static void registerItem(ItemModular item) {
        items.add(item);
    }

    public static void loadModels(ModelBakeEvent event) {

        //        TextureAtlasSprite particleSprite = ModelLoader.defaultTextureGetter().apply(new ResourceLocation(unbaked.resolveTextureName("particle")));
        items.forEach(item -> {
            ModelResourceLocation resourceLocation = new ModelResourceLocation(ItemSwordModular.instance.getRegistryName(), "inventory");
            SimpleBakedModel originalModel = (SimpleBakedModel) event.getModelRegistry().get(resourceLocation);
            try {
                IBakedModel model = loadModel(event.getModelLoader(), originalModel, resourceLocation);
                event.getModelRegistry().put(resourceLocation, model);
            } catch (Exception e) {
                logger.warn(e);
            }
        });
    }

    private static IBakedModel loadModel(ModelLoader modelLoader, SimpleBakedModel originalModel, ModelResourceLocation location) throws Exception {
        BlockModel unbakedModel = (BlockModel) modelLoader.getUnbakedModel(location);

        return new BakedWrapper(originalModel, new ModularOverrideList(modelLoader, unbakedModel));
//        return new BakedWrapper(event.getModelLoader(), ModelLoader.defaultTextureGetter(), ModelRotation.X0_Y0,
//                net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM);
    }
}
