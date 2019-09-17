package se.mickelus.tetra.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.io.FilenameUtils;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.generation.GenerationFeature;
import se.mickelus.tetra.loot.LootEntryDeserializer;
import se.mickelus.tetra.loot.LootPoolDeserializer;
import se.mickelus.tetra.module.ReplacementDefinition;
import se.mickelus.tetra.module.data.*;
import se.mickelus.tetra.module.Priority;
import se.mickelus.tetra.module.schema.Material;
import se.mickelus.tetra.module.schema.SchemaDefinition;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class DataHandler {
    private final File source;
    public final Gson gson;

    private File configDir;

    public static DataHandler instance;

    public DataHandler(File source) {
        this.source = source;
        configDir = Loader.instance().getConfigDir();

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
                .registerTypeAdapter(LootPool.class, new LootPoolDeserializer())
                .registerTypeAdapter(LootEntry.class, new LootEntryDeserializer())
                .registerTypeAdapter(LootEntry.class, new LootEntryDeserializer())
                .registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer())
                .registerTypeAdapter(LootFunction.class, new LootFunctionManager.Serializer())
                .registerTypeAdapter(LootCondition.class, new LootConditionManager.Serializer())
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
        File configOverride = new File (configDir, String.format("%s/%s.json", TetraMod.MOD_ID, path));

        try {
            T data = null;
            if (configOverride.exists()) {
                data = readData(configOverride.toPath(), dataClass);
            } else if (source.isFile()) {
                try (FileSystem fs = FileSystems.newFileSystem(source.toPath(), null)) {
                    data = readData(fs.getPath(pathString), dataClass);
                }
            } else if (source.isDirectory()) {
                data = readData(source.toPath().resolve(pathString), dataClass);
            }

            if (data != null) {
                return data;
            }
        } catch (IOException e) {
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
        File configOverride = new File (configDir, String.format("%s/assets/%s/%s.json", TetraMod.MOD_ID, namespace, path));

        try {
            T asset = null;
            if (configOverride.exists()) {
                asset = readData(configOverride.toPath(), assetClass);
            } else if (source.isFile()) {
                try (FileSystem fs = FileSystems.newFileSystem(source.toPath(), null)) {
                    asset = readData(fs.getPath(pathString), assetClass);
                }
            } else if (source.isDirectory()) {
                asset = readData(source.toPath().resolve(pathString), assetClass);
            }

            if (asset != null) {
                return asset;
            }
        } catch (IOException e) {
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

    public GenerationFeature[] getGenerationFeatures() {
        String pathString = String.format("assets/%s/structures", TetraMod.MOD_ID);

        try {
            GenerationFeature[] features = null;
            if (source.isFile()) {
                try (FileSystem fs = FileSystems.newFileSystem(source.toPath(), null)) {
                    features = getGenerationFeatures(fs.getPath(pathString));
                }
            } else if (source.isDirectory()) {
                features = getGenerationFeatures(source.toPath().resolve(pathString));
            }

            if (features != null) {
                return features;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new GenerationFeature[0];
    }

    private GenerationFeature[] getGenerationFeatures(Path structuresPath) throws IOException {
        if (structuresPath != null && Files.exists(structuresPath)) {
            try (Stream<Path> stream = Files.list(structuresPath)) {
                return stream
                        .filter(path -> FilenameUtils.isExtension(path.toString(), "json"))
                        .map(this::getGenerationFeature)
                        .filter(Objects::nonNull)
                        .toArray(GenerationFeature[]::new);
            }
        }

        return null;
    }

    private GenerationFeature getGenerationFeature(Path path) {

        try (BufferedReader reader = Files.newBufferedReader(path)){
            GenerationFeature generationFeature = gson.fromJson(reader, GenerationFeature.class);

            if (generationFeature != null && generationFeature.location == null) {
                generationFeature.location = new ResourceLocation(TetraMod.MOD_ID, FilenameUtils.removeExtension(path.getFileName().toString()));
            }


            return generationFeature;
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println("Failed to read generation feature from: " + path);
        return null;
    }

    public LootPool[] getExtendedLootPools(ResourceLocation poolLocation) {
        return getAsset(poolLocation.getResourceDomain(), String.format("loot_pools_extended/%s", poolLocation.getResourcePath()), LootPool[].class);

    }
}
