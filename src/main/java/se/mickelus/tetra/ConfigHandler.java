package se.mickelus.tetra;

import java.io.File;


import net.minecraftforge.common.config.Configuration;

public class ConfigHandler {

    static boolean IMPROVE_DIFFICULTY = true;


    public static void init(File file) {
        Configuration config = new Configuration(file);
        config.load();

        IMPROVE_DIFFICULTY = config.getBoolean(
                "improve_difficulty", "generation_settings",
                IMPROVE_DIFFICULTY,
                "Increases the strength of mobs spawned under certain conditions.");

        config.save();
    }
}
