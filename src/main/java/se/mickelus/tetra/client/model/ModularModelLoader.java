package se.mickelus.tetra.client.model;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.data.DataManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
public class ModularModelLoader implements IGeometryLoader<UnresolvedItemModel> {

    private static final Logger logger = LogManager.getLogger();

    private static List<UnresolvedItemModel> newModels = new LinkedList<>();
    private static List<UnresolvedItemModel> models = new LinkedList<>();

    public ModularModelLoader() {
        models.forEach(UnresolvedItemModel::clearCache);
    }

    public static void init() {
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

    public synchronized static void clearCaches() {
        logger.info("Clearing model cache for {} items, let's get bakin'", models.size());
        models.forEach(UnresolvedItemModel::clearCache);
        shuffle();
    }

    private synchronized static void addModel(UnresolvedItemModel model) {
        newModels.add(model);
    }

    @Override
    public UnresolvedItemModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        ItemTransforms cameraTransforms = deserializationContext.deserialize(modelContents.get("display"), ItemTransforms.class);

        if (modelContents.has("variants")) {
            Map<String, ItemTransforms> transformVariants = deserializationContext.deserialize(modelContents.get("variants"),
                    new TypeToken<Map<String, ItemTransforms>>() {
                    }.getType());

            UnresolvedItemModel model = new UnresolvedItemModel(cameraTransforms, transformVariants);
            addModel(model);
            return model;
        }

        UnresolvedItemModel model = new UnresolvedItemModel(cameraTransforms);
        addModel(model);
        return model;
    }
}
