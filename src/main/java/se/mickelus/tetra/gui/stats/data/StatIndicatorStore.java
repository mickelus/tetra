package se.mickelus.tetra.gui.stats.data;

import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.stats.bar.GuiStatIndicator;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class StatIndicatorStore implements ResourceManagerReloadListener {
    private static final Logger logger = LogManager.getLogger();
    public static StatIndicatorStore instance;
    private Map<ResourceLocation, GuiStatIndicator> indicators = Collections.emptyMap();

    public StatIndicatorStore() {
        instance = this;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        indicators = prepareIndicators();
        logger.info("Loaded {} stat indicators", this.indicators.size());
    }

    public GuiStatIndicator[] getIndicatorsIn(ResourceLocation resourceLocation) {
        return indicators.entrySet().stream()
                .filter(entry -> resourceLocation.getNamespace().equals(entry.getKey().getNamespace())
                        && entry.getKey().getPath().startsWith(resourceLocation.getPath()))
                .map(Map.Entry::getValue)
                .toArray(GuiStatIndicator[]::new);
    }

    private static Map<ResourceLocation, GuiStatIndicator> prepareIndicators() {
        return Minecraft.getInstance().getResourceManager().listResources("stat_indicators", rl -> rl.getPath().endsWith(".json")).entrySet().stream()
                .filter(entry -> TetraMod.MOD_ID.equals(entry.getKey().getNamespace()))
                .map(entry -> Pair.of(trimResourceLocation(entry.getKey()), parseBar(entry.getKey(), entry.getValue())))
                .filter(pair -> pair.getRight() != null)
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    private static ResourceLocation trimResourceLocation(ResourceLocation resourceLocation) {
        return new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath().substring(16));
    }

    @Nullable
    private static GuiStatIndicator parseBar(ResourceLocation resourceLocation, Resource resource) {
        try (BufferedReader reader = resource.openAsReader()) {
            return GsonHelper.fromJson(StatRegistry.gson, reader, GuiStatIndicator.class);
        } catch (IOException | JsonParseException e) {
            logger.error("Failed to parse stat indicator from '{}': {}", resourceLocation, e);
        }

        return null;
    }

}
