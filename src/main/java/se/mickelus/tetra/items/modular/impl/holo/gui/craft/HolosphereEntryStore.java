package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

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
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.modular.IModularItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class HolosphereEntryStore implements ResourceManagerReloadListener {
    protected static final String jsonExtension = ".json";
    private static final String directory = "holosphere_entries";
    private static final Logger logger = LogManager.getLogger();
    public static HolosphereEntryStore instance;
    private Map<String, HolosphereEntryData> entries = Collections.emptyMap();

    private Runnable listener;

    public HolosphereEntryStore() {
        instance = this;
    }

    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.entries = parsePentries();
        logger.info("Loaded {} holosphere entries", this.entries.size());

        if (listener != null) {
            listener.run();
        }
    }

    public Map<String, HolosphereEntryData> getEntries() {
        return entries;
    }

    private Map<String, HolosphereEntryData> parsePentries() {
        return Minecraft.getInstance().getResourceManager().listResources(directory, rl -> rl.getPath().endsWith(jsonExtension)).entrySet().stream()
                .filter(entry -> TetraMod.MOD_ID.equals(entry.getKey().getNamespace()))
                .map(entry -> parseEntry(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .filter(entry -> entry.item instanceof IModularItem)
                .collect(Collectors.toMap(entry -> entry.key, entry -> entry));
    }

    private HolosphereEntryData parseEntry(ResourceLocation resourceLocation, Resource resource) {
        try (BufferedReader reader = resource.openAsReader()) {
            HolosphereEntryData result = GsonHelper.fromJson(DataManager.gson, reader, HolosphereEntryData.class);
            result.key = resourceLocation.getPath().substring(directory.length() + 1, resourceLocation.getPath().length() - jsonExtension.length());
            return result;
        } catch (IOException | JsonParseException e) {
            logger.warn("Failed to parse holosphere entry from '{}': {}", resourceLocation, e);
        }
        return null;
    }
}
