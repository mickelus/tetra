package se.mickelus.tetra;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import se.mickelus.tetra.module.CapabilityData;
import se.mickelus.tetra.module.GlyphData;
import se.mickelus.tetra.module.Priority;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataHandler {
    private final File source;
    private final Gson gson;

    public static DataHandler instance;

    public DataHandler(File source) {
        this.source = source;
        gson = new GsonBuilder()
                .registerTypeAdapter(CapabilityData.class, new CapabilityData.CapabilityTypeAdapter())
                .registerTypeAdapter(GlyphData.class, new GlyphData.GlyphTypeAdapter())
                .registerTypeAdapter(Priority.class, new Priority.PriorityAdapter())
                .create();

        instance = this;
    }

    public <T> T getModuleData(String moduleKey, Class<T> dataClass) {
        return getData(String.format("modules/%s", moduleKey), dataClass);
    }

    public <T> T getData(String path, Class<T> dataClass) {
        String pathString = String.format("data/%s/%s.json", TetraMod.MOD_ID, path);
        try {
            Path fPath = null;

            if (source.isFile()) {
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
        System.err.printf("Could not read data from '%s'. Initializing from empty array.\n");
        return gson.fromJson("[]", dataClass);
    }
}
