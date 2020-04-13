package se.mickelus.tetra.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.data.DataManager;

import java.util.LinkedList;
import java.util.List;

public class ModularModelLoader implements IModelLoader<ModularItemModel> {

    private static final Logger logger = LogManager.getLogger();

    private static List<ModularItemModel> models = new LinkedList<>();

    public ModularModelLoader() {
        // module data is the last data store that contains model information
        DataManager.moduleData.onReload(ModularModelLoader::clearCaches);
    }

    public static void clearCaches() {
        logger.info("Clearing item model cache, let's get bakin'");
        models.forEach(ModularItemModel::clearCache);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        logger.info("Clearing item models");
        models.clear();
    }

    @Override
    public ModularItemModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        ItemCameraTransforms cameraTransforms = deserializationContext.deserialize(modelContents.get("display"), ItemCameraTransforms.class);
        ModularItemModel model = new ModularItemModel(cameraTransforms);
        models.add(model);
        return model;
    }
}
