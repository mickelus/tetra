package se.mickelus.tetra.client.model;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.data.DataManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
@ParametersAreNonnullByDefault
public class ModularModelLoader implements IModelLoader<ModularItemModel> {

    private static final Logger logger = LogManager.getLogger();

    private static List<ModularItemModel> newModels = new LinkedList<>();
    private static List<ModularItemModel> models = new LinkedList<>();

    public ModularModelLoader() {
        // module data is the last data store that contains model information
        DataManager.instance.moduleData.onReload(ModularModelLoader::clearCaches);
    }

    /**
     * Hack to shuffle models around since onResourceManagerReload is called after all models are loaded and there's no context available to tell them
     * apart, so that override caches can be cleared when data is reloaded.
     */
    private static void shuffle() {
        if (!newModels.isEmpty()) {
            models = newModels;
            newModels = new LinkedList<>();
        }
    }

    public static void clearCaches() {
        shuffle();
        logger.info("Clearing model cache for {} items, let's get bakin'", models.size());
        models.forEach(ModularItemModel::clearCache);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        logger.info("Reloading item models, old: {}, new: {}", models.size(), newModels.size());
        shuffle();
    }

    @Override
    public ModularItemModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        ItemTransforms cameraTransforms = deserializationContext.deserialize(modelContents.get("display"), ItemTransforms.class);

        if (modelContents.has("variants")) {
            Map<String, ItemTransforms> transformVariants = deserializationContext.deserialize(modelContents.get("variants"),
                    new TypeToken<Map<String, ItemTransforms>>(){}.getType());

            ModularItemModel model = new ModularItemModel(cameraTransforms, transformVariants);
            newModels.add(model);
            return model;
        }

        ModularItemModel model = new ModularItemModel(cameraTransforms);
        newModels.add(model);
        return model;
    }
}
