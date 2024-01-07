package se.mickelus.tetra.client;

import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.ToolAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.data.GlyphData;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ToolActionIconStore implements ResourceManagerReloadListener {
    protected static final String jsonExtension = ".json";
    private static final String directory = "tool_actions";
    private static final Logger logger = LogManager.getLogger();
    public static ToolActionIconStore instance;
    private Map<ToolAction, GlyphData> icons = Collections.emptyMap();

    public ToolActionIconStore() {
        instance = this;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.icons = prepareIcons();
        logger.info("Loaded {} tool action icons", this.icons.size());
    }

    public GlyphData getIcon(ToolAction action) {
        return icons.get(action);
    }

    private Map<ToolAction, GlyphData> prepareIcons() {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        return resourceManager.listResources(directory, rl -> rl.getPath().endsWith(jsonExtension)).entrySet().stream()
                .filter(entry -> TetraMod.MOD_ID.equals(entry.getKey().getNamespace()))
                .collect(
                        HashMap::new,
                        (map, entry) -> map.put(getAction(entry.getKey()), getGlyph(entry.getKey(), entry.getValue())),
                        HashMap::putAll);
    }

    private ToolAction getAction(ResourceLocation resourceLocation) {
        String path = resourceLocation.getPath();
        return ToolAction.get(path.substring(directory.length() + 1, path.length() - jsonExtension.length()));
    }

    private GlyphData getGlyph(ResourceLocation resourceLocation, Resource resource) {
        try (BufferedReader reader = resource.openAsReader()) {
            return Optional.of(GsonHelper.fromJson(DataManager.gson, reader, GlyphData.class)).orElse(null);
        } catch (IOException | JsonParseException e) {
            logger.warn("Failed to parse tool action icon from '{}': {}", resourceLocation, e);
        }
        return null;
    }
}
