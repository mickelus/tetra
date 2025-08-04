package se.mickelus.tetra.gui.stats.data;

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
import se.mickelus.tetra.gui.stats.sorting.IStatSorter;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;

public class StatSorterStore implements ResourceManagerReloadListener {
    private static final Logger logger = LogManager.getLogger();
    public static StatSorterStore instance;
    private IStatSorter[] sorters = new IStatSorter[0];

    public StatSorterStore() {
        instance = this;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        sorters = prepare();
        logger.info("Loaded {} stat sorters", this.sorters.length);
    }

    private static IStatSorter[] prepare() {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        return resourceManager.listResources("stat_sorters", rl -> rl.getPath().endsWith(".json")).entrySet().stream()
                .filter(entry -> TetraMod.MOD_ID.equals(entry.getKey().getNamespace()))
                .map(entry -> parseSorter(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .toArray(IStatSorter[]::new);
    }

    @Nullable
    private static IStatSorter parseSorter(ResourceLocation resourceLocation, Resource resource) {
        try (BufferedReader reader = resource.openAsReader()) {
            return GsonHelper.fromJson(StatRegistry.gson, reader, IStatSorter.class);
        } catch (IOException | JsonParseException e) {
            logger.error("Failed to parse stat sorter data from '{}': {}", resourceLocation, e);
        }

        return null;
    }

    public IStatSorter[] getSorters() {
        return sorters;
    }
}

