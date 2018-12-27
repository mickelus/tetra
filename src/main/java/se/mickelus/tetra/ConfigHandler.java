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
    public static String[] geode_contents = new String[] {
            "minecraft:iron_nugget", "9", "30",
            "minecraft:gold_nugget", "4", "20",
            "minecraft:redstone", "6", "20",
            "minecraft:flint", "3", "20",
            "minecraft:diamond", "1", "2",
            "minecraft:emerald", "1", "1",
    };

    public static boolean feature_generate = false;
    public static int max_feature_depth = 8;

    public static boolean workbenchDropTable = true;

    @Mod.EventBusSubscriber
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(TetraMod.MOD_ID)) {
                ConfigManager.sync(TetraMod.MOD_ID, Config.Type.INSTANCE);
                ItemGeode.instance.initContent();
            }
        }
    }
}
