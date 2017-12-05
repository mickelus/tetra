package se.mickelus.tetra;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import se.mickelus.tetra.module.CapabilityData;
import se.mickelus.tetra.module.GlyphData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class DataHandler {
    private final File source;
    private final Gson gson;

    public static DataHandler instance;

    public DataHandler(File source) {
        this.source = source;
        gson = new GsonBuilder()
                .registerTypeAdapter(CapabilityData.class, new CapabilityData.CapabilityTypeAdapter())
                .registerTypeAdapter(GlyphData.class, new GlyphData.GlyphTypeAdapter())
                .create();

        instance = this;
    }

    public <T> T getModuleData(String moduleKey, Class<T> dataClass) {
        File file = new File(source, String.format("data/%s/modules/%s.json", TetraMod.MOD_ID, moduleKey));

        try {
            return gson.fromJson(new InputStreamReader(new FileInputStream(file)), dataClass);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return gson.fromJson("[]", dataClass);
    }
}
