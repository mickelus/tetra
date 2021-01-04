package se.mickelus.tetra.compat.botania;

import net.minecraftforge.fml.ModList;

public class BotaniaCompat {
    public static final String modId = "botania";
    public static final Boolean isLoaded = ModList.get().isLoaded(modId);

    public static void clientInit() {
        if (isLoaded) {
            ManaRepair.clientInit();
        }
    }
}
