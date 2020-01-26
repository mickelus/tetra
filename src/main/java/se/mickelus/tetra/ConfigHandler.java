package se.mickelus.tetra;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import se.mickelus.tetra.items.duplex_tool.ItemDuplexToolModular;
import se.mickelus.tetra.items.sword.ItemSwordModular;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class ConfigHandler {
    private static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec spec;

    public static ForgeConfigSpec.BooleanValue development;

    public static ForgeConfigSpec.IntValue geodeDensity;

    public static ForgeConfigSpec.BooleanValue generateFeatures;
    public static ForgeConfigSpec.IntValue maxFeatureDepth;

    public static ForgeConfigSpec.BooleanValue moduleProgression;
    public static ForgeConfigSpec.IntValue settleLimitBase;
    public static ForgeConfigSpec.DoubleValue settleLimitLevelMultiplier;
    public static ForgeConfigSpec.DoubleValue settleLimitDurabilityMultiplier;

    public static ForgeConfigSpec.IntValue honeSwordBase;
    public static ForgeConfigSpec.IntValue honeSwordIntegrityMultiplier;

    public static ForgeConfigSpec.IntValue honeDuplexBase;
    public static ForgeConfigSpec.IntValue honeDuplexIntegrityMultiplier;


    static {
        // misc config
        builder.push("misc");

        development = builder
                .comment("Enables commands & data reloading functionality useful for development, has a negative impact on performance")
                .worldRestart()
                .define("development", false);

        builder.pop();

        // worldgen config
        builder
                .comment("World generation settings")
                .push("worldgen");

        geodeDensity = builder
                .comment("The number of geodes that should generate per chunk, set to 0 to disable")
                .worldRestart()
                .defineInRange("geode_density", 120, 0, 65536);

        generateFeatures = builder
                .comment("Generates features in the world, further configuration available in \"tetra/data/geode/\"")
                .worldRestart()
                .define("features", true);

        maxFeatureDepth = builder
                .comment("Used to limit how deep the feature generator will recurse into feature children, helps to avoid recursive loops and cascading worldgen in 3d-party generation features")
                .defineInRange("feature_depth", 8, 0, 64);

        builder.pop();

        // module progression config
        builder
                .comment("Allows tetra items to \"level up\" after being used a certain amount of times, allowing the player to choose from different ways to \"hone\" 1 module on the item. Major modules also settle after some time, increasing its integrity")
                .push("module_progression");

        moduleProgression = builder
                .define("enabled", true);

        settleLimitBase = builder
                .comment("The base value for number of uses required for a module to settle")
                .defineInRange("settle_base", 200, Integer.MIN_VALUE, Integer.MAX_VALUE);

        settleLimitLevelMultiplier = builder
                .comment("Level multiplier for settling limit, a value of 3 would cause a module that has settled once to require 3x as many uses before it settles again")
                .defineInRange("settle_level_multiplier", 3d, Double.MIN_VALUE, Double.MAX_VALUE);

        settleLimitDurabilityMultiplier = builder
                .comment("Durability multiplier for settling limit, a value of 1 would cause a module with 75 durability to require an additional 75 uses before it settles")
                .defineInRange("settle_durability_multiplier", 0.5d, Double.MIN_VALUE, Double.MAX_VALUE);

        honeSwordBase = builder
                .comment("The base value for number of uses required before a sword can be honed")
                .defineInRange("hone_sword_base", 120, Integer.MIN_VALUE, Integer.MAX_VALUE);

        honeSwordIntegrityMultiplier = builder
                .comment("Integrity multiplier for sword honing, a value of 2 would cause a sword which uses 3 integrity to require 2*3 times as many uses before it can be honed")
                .defineInRange("hone_sword_integrity_multiplier", 75, Integer.MIN_VALUE, Integer.MAX_VALUE);

        honeDuplexBase = builder
                .comment("The base value for number of uses required before a tool can be honed")
                .defineInRange("hone_duplex_base", 190, Integer.MIN_VALUE, Integer.MAX_VALUE);

        honeDuplexIntegrityMultiplier = builder
                .comment("Integrity multiplier for tool honing, a value of 2 would cause a sword which uses 3 integrity to require 2*3 times as many uses before it can be honed")
                .defineInRange("hone_duplex_integrity_multiplier", 90, Integer.MIN_VALUE, Integer.MAX_VALUE);

        builder.pop();

        spec = builder.build();
    }

    public static void setup() {
        // this is slightly more complicated than just calling ModLoadingContext.registerConfig but it allows us to preserve insertion order
        // which makes the config easier to read
        final CommentedFileConfig configData = CommentedFileConfig.builder(FMLPaths.CONFIGDIR.get().resolve("tetra.toml"))
                .sync()
                .autosave()
                .preserveInsertionOrder()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);

    }

    /**
     * Will only be called if ModLoadingContext.registerConfig is used to register the config as a server config
     * @param configEvent
     */
    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        ItemSwordModular.instance.updateConfig(honeSwordBase.get(), honeSwordIntegrityMultiplier.get());
        ItemDuplexToolModular.instance.updateConfig(honeSwordBase.get(), honeSwordIntegrityMultiplier.get());
    }

    /**
     * Will only be called if ModLoadingContext.registerConfig is used to register the config as a server config
     * @param configEvent
     */
    @SubscribeEvent
    public static void onReload(final ModConfig.ConfigReloading configEvent) {
        ItemSwordModular.instance.updateConfig(honeSwordBase.get(), honeSwordIntegrityMultiplier.get());
        ItemDuplexToolModular.instance.updateConfig(honeSwordBase.get(), honeSwordIntegrityMultiplier.get());
    }
}
