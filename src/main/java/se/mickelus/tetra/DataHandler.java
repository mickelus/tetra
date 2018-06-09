package se.mickelus.tetra;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraftforge.fml.common.Loader;
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

public class DataHandler {
    private final File source;
    private final Gson gson;

    private File configDir;

    public static DataHandler instance;

    public DataHandler(File source) {
        this.source = source;
        configDir = Loader.instance().getConfigDir();

        // todo: use the same naming for all deserializers?
        gson = new GsonBuilder()
                .registerTypeAdapter(CapabilityData.class, new CapabilityData.Deserializer())
                .registerTypeAdapter(EffectData.class, new EffectData.Deserializer())
                .registerTypeAdapter(GlyphData.class, new GlyphData.GlyphTypeAdapter())
                .registerTypeAdapter(Priority.class, new Priority.PriorityAdapter())
                .registerTypeAdapter(ItemPredicate.class, new PredicateDeserializer())
                .registerTypeAdapter(Material.class, new Material.MaterialDeserializer())
                .registerTypeAdapter(ReplacementDefinition.class, new ReplacementDefinition.ReplacementDeserializer())
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
        return getData(String.format("replacements/%s", path), ReplacementDefinition[].class);
    }

    public <T> T getData(String path, Class<T> dataClass) {
        String pathString = String.format("data/%s/%s.json", TetraMod.MOD_ID, path);
        try {
            Path fPath = null;

            File configOverride = new File (configDir, String.format("%s/%s.json", TetraMod.MOD_ID, path));
            if (configOverride.exists()) {
                fPath = configOverride.toPath();
            } else if (source.isFile()) {
                FileSystem fs = FileSystems.newFileSystem(source.toPath(), null);
                fPath = fs.getPath(pathString);
            } else if (source.isDirectory()) {
                fPath = source.toPath().resolve(pathString);
            }

            if (fPath != null && Files.exists(fPath)) {
                BufferedReader reader = Files.newBufferedReader(fPath);
                return gson.fromJson(reader, dataClass);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.printf("Could not read data from '%s'. Initializing from empty array.\n", path);
        return gson.fromJson("[]", dataClass);
    }
}
