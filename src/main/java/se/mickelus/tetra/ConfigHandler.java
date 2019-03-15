package se.mickelus.tetra;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import se.mickelus.tetra.blocks.geode.ItemGeode;

@Config(modid = TetraMod.MOD_ID, name = TetraMod.MOD_ID + "/" + TetraMod.MOD_ID)
public class ConfigHandler {

    public static boolean development = false;

    public static boolean geode_generate = true;

    public static boolean generate_features = true;
    public static int max_feature_depth = 8;

    public static boolean workbenchDropTable = true;

    @Mod.EventBusSubscriber
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(TetraMod.MOD_ID)) {
                ConfigManager.sync(TetraMod.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}
