package se.mickelus.tetra.compat.botania;

import net.minecraftforge.fml.ModList;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class BotaniaCompat {
    public static final String modId = "botania";
    public static final Boolean isLoaded = ModList.get().isLoaded(modId);

    public static void clientInit() {
        if (isLoaded) {
            ManaRepair.clientInit();
        }
    }
}
