package se.mickelus.tetra.data;

import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.forgespi.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.TetraMod;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class DataStore<V> extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
    private static final Logger logger = LogManager.getLogger();
    protected static final int jsonExtLength = ".json".length();

    protected Gson gson;
    protected String directory;
    protected Class<V> dataClass;

    protected Map<ResourceLocation, JsonElement> rawData;
    protected Map<ResourceLocation, V> dataMap;
    protected List<Runnable> listeners;

    public DataStore(Gson gson, String directory, Class<V> dataClass) {
        this.gson = gson;
        this.directory = directory;

        this.dataClass = dataClass;

        rawData = Collections.emptyMap();
        dataMap = Collections.emptyMap();

        listeners = new LinkedList<>();
    }

    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        logger.debug("Reading data for {} data store...", directory);
        Map<ResourceLocation, JsonElement> map = Maps.newHashMap();
        int i = this.directory.length() + 1;

        for(ResourceLocation fullLocation : resourceManager.listResources(directory, rl -> rl.endsWith(".json"))) {
            if (!TetraMod.MOD_ID.equals(fullLocation.getNamespace())) {
                continue;
            }

            String path = fullLocation.getPath();
            ResourceLocation location = new ResourceLocation(fullLocation.getNamespace(), path.substring(i, path.length() - jsonExtLength));

            try (
                    Resource resource = resourceManager.getResource(fullLocation);
                    InputStream inputStream = resource.getInputStream();
                    Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            ) {
                JsonElement json;

                if (dataClass.isArray()) {
                    json = GsonHelper.fromJson(gson, reader, JsonArray.class);
                } else {
                    json = GsonHelper.fromJson(gson, reader, JsonElement.class);
                }

                if (json != null) {
                    if (shouldLoad(json)) {
                        JsonElement duplicate = map.put(location, json);
                        if (duplicate != null) {
                            throw new IllegalStateException("Duplicate data ignored with ID " + location);
                        }
                    } else {
                        logger.debug("Skipping data '{}' due to condition", fullLocation);
                    }
                } else {
                    logger.error("Couldn't load data from '{}' as it's null or empty", fullLocation);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
                logger.error("Couldn't parse data '{}' from '{}'", location, fullLocation, jsonparseexception);
            }
        }

        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> splashList, ResourceManager resourceManager, ProfilerFiller profiler) {
        rawData = splashList;

        // PacketHandler dependencies get upset when called upon before the server has started properly
        if (Environment.get().getDist().isDedicatedServer() && ServerLifecycleHooks.getCurrentServer() != null) {
            TetraMod.packetHandler.sendToAllPlayers(new UpdateDataPacket(directory, rawData));
        }

        parseData(rawData);
    }

    public void sendToPlayer(ServerPlayer player) {
        TetraMod.packetHandler.sendTo(new UpdateDataPacket(directory, rawData), player);
    }

    public void loadFromPacket(Map<ResourceLocation, String> data) {
        Map<ResourceLocation, JsonElement> splashList = data.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            if (dataClass.isArray()) {
                                return GsonHelper.fromJson(gson, entry.getValue(), JsonArray.class);
                            } else {
                                return GsonHelper.fromJson(gson, entry.getValue(), JsonElement.class);
                            }
                        }
                ));

        parseData(splashList);
    }

    public void parseData(Map<ResourceLocation, JsonElement> splashList) {
        logger.info("Loaded {} {}", String.format("%3d", splashList.values().size()), directory);
        dataMap = splashList.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> gson.fromJson(entry.getValue(), dataClass)
                ));

        processData();

        listeners.forEach(Runnable::run);
    }

    protected boolean shouldLoad(JsonElement json) {
        if (json.isJsonArray()) {
            JsonArray arr = json.getAsJsonArray();
            if (arr.size() > 0)
                json = arr.get(0);
        }

        if (!json.isJsonObject()) {
            return true;
        }

        JsonObject jsonObject = json.getAsJsonObject();
        return !jsonObject.has("conditions") || CraftingHelper.processConditions(GsonHelper.getAsJsonArray(jsonObject, "conditions"));
    }

    protected void processData() {

    }

    public Map<ResourceLocation, JsonElement> getRawData() {
        return rawData;
    }

    public String getDirectory() {
        return directory;
    }

    /**
     * Get the resource at the given location from the set of resources that this listener is managing
     *
     * @param resourceLocation A resource location
     * @return An object matching the type of this listener, or null if none exists at the given location
     */
    public V getData(ResourceLocation resourceLocation) {
        return dataMap.get(resourceLocation);
    }

    /**
     * @return all data from this store.
     */
    public Map<ResourceLocation, V> getData() {
        return dataMap;
    }

    /**
     * Get all resources (if any) that are within the directory denoted by the provided resource location
     *
     * @param resourceLocation
     * @return
     */
    public Collection<V> getDataIn(ResourceLocation resourceLocation) {
        return getData().entrySet().stream()
                .filter(entry -> resourceLocation.getNamespace().equals(entry.getKey().getNamespace())
                        && entry.getKey().getPath().startsWith(resourceLocation.getPath()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Listen to changes on resources in this store
     *
     * @param callback A runnable that is to be called when the store is reloaded
     */
    public void onReload(Runnable callback) {
        listeners.add(callback);
    }
}
