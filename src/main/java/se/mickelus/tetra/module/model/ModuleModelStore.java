package se.mickelus.tetra.module.model;

import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.stats.data.StatRegistry;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;

public class ModuleModelStore implements ResourceManagerReloadListener {
    private static final Logger logger = LogManager.getLogger();
    public static ModuleModelStore instance;
    private IModuleModel[] statBars = new IModuleModel[0];

    public ModuleModelStore() {
        instance = this;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        statBars = prepareModels();
        logger.info("Loaded {} module models", this.statBars.length);
    }

    public IModuleModel[] getBars() {
        return statBars;
    }

    private static IModuleModel[] prepareModels() {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        return resourceManager.listResources("module_model", rl -> rl.getPath().endsWith(".json")).entrySet().stream()
                .filter(entry -> TetraMod.MOD_ID.equals(entry.getKey().getNamespace()))
                .map(entry -> parseModel(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .toArray(IModuleModel[]::new);
    }

    @Nullable
    private static IModuleModel parseModel(ResourceLocation resourceLocation, Resource resource) {
        try (BufferedReader reader = resource.openAsReader()) {
            return GsonHelper.fromJson(StatRegistry.gson, reader, IModuleModel.class);
        } catch (IOException | JsonParseException e) {
            logger.error("Failed to parse module model data from '{}': {}", resourceLocation, e.getMessage());
            logger.error(e);
        }

        return null;
    }

}
