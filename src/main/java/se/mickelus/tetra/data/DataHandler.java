package se.mickelus.tetra.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.ILootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.fml.loading.FMLPaths;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.module.Priority;
import se.mickelus.tetra.module.ReplacementDefinition;
import se.mickelus.tetra.module.data.CapabilityData;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.data.SynergyData;
import se.mickelus.tetra.module.schema.Material;
import se.mickelus.tetra.module.schema.SchemaDefinition;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Arrays;

public class DataHandler {
    public final Gson gson;

    private Path configDir;

    public static DataHandler instance;

    public DataHandler() {
        configDir = FMLPaths.CONFIGDIR.get().resolve("config");

        // todo: use the same naming for all deserializers?
        gson = new GsonBuilder()
                .registerTypeAdapter(CapabilityData.class, new CapabilityData.Deserializer())
                .registerTypeAdapter(EffectData.class, new EffectData.Deserializer())
                .registerTypeAdapter(GlyphData.class, new GlyphDeserializer())
                .registerTypeAdapter(Priority.class, new Priority.PriorityAdapter())
                .registerTypeAdapter(ItemPredicate.class, new ItemPredicateDeserializer())
                .registerTypeAdapter(PropertyMatcher.class, new PropertyMatcherDeserializer())
                .registerTypeAdapter(Material.class, new Material.MaterialDeserializer())
                .registerTypeAdapter(ReplacementDefinition.class, new ReplacementDeserializer())
                .registerTypeAdapter(BlockPos.class, new BlockPosDeserializer())
                .registerTypeAdapter(Block.class, new BlockDeserializer())
                .registerTypeAdapter(ResourceLocation.class, new ResourceLocationDeserializer())
                .registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer())
                .registerTypeAdapter(ILootFunction.class, new LootFunctionManager.Serializer())
                .registerTypeAdapter(ILootCondition.class, new LootConditionManager.Serializer())
                .registerTypeAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer())
                .create();

        instance = this;
    }

    public <T> T getModuleData(String moduleKey, Class<T> dataClass) {
        return getData(String.format("modules/%s", moduleKey), dataClass);
    }

    public SchemaDefinition[] getSchemaDefinitions(String schemaName) {
        return getData(String.format("schemas/%s", schemaName), SchemaDefinition[].class);
    }

    public SynergyData[] getSynergyData(String path) {
        SynergyData[] data = getData(path, SynergyData[].class);
        for (SynergyData entry : data) {
            Arrays.sort(entry.moduleVariants);
            Arrays.sort(entry.modules);
        }
        return data;
    }

    public ReplacementDefinition[] getReplacementDefinition(String path) {
        return Arrays.stream(getData(String.format("replacements/%s", path), ReplacementDefinition[].class))
                .filter(replacementDefinition -> replacementDefinition.predicate != null)
                .toArray(ReplacementDefinition[]::new);
    }

    public <T> T getData(String path, Class<T> dataClass) {
        String pathString = String.format("data/%s/%s.json", TetraMod.MOD_ID, path);
        Path configOverride = configDir.resolve(String.format("%s/%s.json", TetraMod.MOD_ID, path));
        try {
            T data = null;
            if (Files.exists(configOverride)) {
                data = readData(configOverride, dataClass);
            } else {
                URL url = ClassLoader.getSystemClassLoader().getResource(pathString);
                if (url != null) {
                    data = readData(Paths.get(url.toURI()), dataClass);
                }
            }

            if (data != null) {
                return data;
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        System.err.printf("Could not read data from '%s'. Initializing from empty array.\n", path);
        return gson.fromJson("[]", dataClass);
    }

    public <T> T getAsset(String path, Class<T> assetClass) {
        return getAsset(TetraMod.MOD_ID, path, assetClass);
    }

    public <T> T getAsset(String namespace, String path, Class<T> assetClass) {
        String pathString = String.format("assets/%s/%s.json", namespace, path);
        Path configOverride = configDir.resolve(String.format("%s/assets/%s/%s.json", TetraMod.MOD_ID, namespace, path));
        try {
            T asset = null;
            if (Files.exists(configOverride)) {
                asset = readData(configOverride, assetClass);
            } else {
                URL url = ClassLoader.getSystemClassLoader().getResource(pathString);
                if (url != null) {
                    asset = readData(Paths.get(url.toURI()), assetClass);
                }
            }

            if (asset != null) {
                return asset;
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        System.err.printf("Could not read assets from '%s'. Initializing from empty array.\n", path);
        return gson.fromJson("[]", assetClass);
    }

    private <T> T readData(Path path, Class<T> dataClass) throws IOException {
        if (path != null && Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                return gson.fromJson(reader, dataClass);
            }
        }

        return null;
    }

    public LootPool[] getExtendedLootPools(ResourceLocation poolLocation) {
        return getAsset(poolLocation.getNamespace(), String.format("loot_pools_extended/%s", poolLocation.getPath()), LootPool[].class);

    }
}
