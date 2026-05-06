package se.mickelus.tetra;

import net.neoforged.fml.ModContainer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.items.modular.impl.ModularDoubleHeadedItem;
import se.mickelus.tetra.items.modular.impl.ModularSingleHeadedItem;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.items.modular.impl.crossbow.ModularCrossbowItemImpl;
import se.mickelus.tetra.items.modular.impl.shield.ModularShieldItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
@EventBusSubscriber(modid = TetraMod.MOD_ID)
public class ConfigHandler {
    private static final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

    public static final int HONE_SWORD_BASE_DEFAULT = 110;
    public static final int HONE_SWORD_INTEGRITY_MULTIPLIER_DEFAULT = 65;
    public static final int HONE_DOUBLE_BASE_DEFAULT = 140;
    public static final int HONE_DOUBLE_INTEGRITY_MULTIPLIER_DEFAULT = 75;
    public static final int HONE_SINGLE_BASE_DEFAULT = 120;
    public static final int HONE_SINGLE_INTEGRITY_MULTIPLIER_DEFAULT = 60;
    public static final int HONE_BOW_BASE_DEFAULT = 48;
    public static final int HONE_BOW_INTEGRITY_MULTIPLIER_DEFAULT = 32;
    public static final int HONE_CROSSBOW_BASE_DEFAULT = 48;
    public static final int HONE_CROSSBOW_INTEGRITY_MULTIPLIER_DEFAULT = 32;
    public static final int HONE_SHIELD_BASE_DEFAULT = 48;
    public static final int HONE_SHIELD_INTEGRITY_MULTIPLIER_DEFAULT = 32;

    public static ModConfigSpec spec;

    public static ModConfigSpec.BooleanValue development;
    public static ModConfigSpec.BooleanValue toolbeltCurioOnly;

    public static ModConfigSpec.ConfigValue<List<? extends String>> experimentalFeatures;
    public static ModConfigSpec.ConfigValue<List<? extends String>> disabledFeatures;

    public static ModConfigSpec.BooleanValue moduleProgression;
    public static ModConfigSpec.IntValue settleLimitBase;
    public static ModConfigSpec.DoubleValue settleLimitLevelMultiplier;
    public static ModConfigSpec.DoubleValue settleLimitDurabilityMultiplier;

    public static ModConfigSpec.DoubleValue magicCapacityMultiplier;

    public static ModConfigSpec.IntValue honeSwordBase;
    public static ModConfigSpec.IntValue honeSwordIntegrityMultiplier;

    public static ModConfigSpec.IntValue honedoubleBase;
    public static ModConfigSpec.IntValue honedoubleIntegrityMultiplier;

    public static ModConfigSpec.IntValue honeSingleBase;
    public static ModConfigSpec.IntValue honeSingleIntegrityMultiplier;

    public static ModConfigSpec.IntValue honeBowBase;
    public static ModConfigSpec.IntValue honeBowIntegrityMultiplier;

    public static ModConfigSpec.IntValue honeCrossbowBase;
    public static ModConfigSpec.IntValue honeCrossbowIntegrityMultiplier;

    public static ModConfigSpec.IntValue honeShieldBase;
    public static ModConfigSpec.IntValue honeShieldIntegrityMultiplier;

    public static ModConfigSpec.BooleanValue enableBow;
    public static ModConfigSpec.BooleanValue enableCrossbow;
    public static ModConfigSpec.BooleanValue enableSingle;
    public static ModConfigSpec.BooleanValue enableShield;

    public static ModConfigSpec.BooleanValue enableGlint;

    public static ModConfigSpec.BooleanValue enableExtractor;

    static {
        // misc config
        builder.push("misc");

        enableBow = builder
                .comment("Enable modular bows")
                .worldRestart()
                .define("bow", true);

        enableSingle = builder
                .comment("Enable modular single headed implements")
                .worldRestart()
                .define("single_headed", true);

        enableShield = builder
                .comment("Enable modular shields")
                .worldRestart()
                .define("shield", true);

        enableGlint = builder
                .comment("Enables the enchantment glint rendering on modular items")
                .define("glint", true);

        development = builder
                .comment("Enables commands & data reloading functionality useful for development, has a negative impact on performance")
                .worldRestart()
                .define("development", false);

        toolbeltCurioOnly = builder
                .comment("If enabled and Curios is installed, Toolbelts will only work in the Curio belt slot")
                .define("toolbelt_curio_only", false);

        experimentalFeatures = builder.comment("Features that are considered experimental can be listed here to enable them")
                .defineListAllowEmpty("experimental_features", Collections.emptyList(), o -> FeatureFlag.matchesAnyKey(o));

        disabledFeatures = builder.comment("Features can be listed here to disable them")
                .defineListAllowEmpty("disabled_features", Collections.emptyList(), o -> FeatureFlag.matchesAnyKey(o));

        magicCapacityMultiplier = builder
                .comment("Multiplier for magic capacity gains, increasing this may be useful when having a large set enchantments added by other " +
                        "mods")
                .defineInRange("magic_cap_multiplier", 1, 0, Double.MAX_VALUE);

        builder.pop();

        // module progression config
        builder
                .comment("Allows tetra items to \"level up\" after being used a certain amount of times, allowing the player to choose from " +
                        "different ways to \"hone\" 1 module on the item. Major modules also settle after some time, increasing its integrity")
                .push("module_progression");

        moduleProgression = builder
                .define("enabled", true);

        settleLimitBase = builder
                .comment("The base value for number of uses required for a module to settle")
                .defineInRange("settle_base", 270, Integer.MIN_VALUE, Integer.MAX_VALUE);

        settleLimitLevelMultiplier = builder
                .comment("Level multiplier for settling limit, a value of 3 would cause a module that has settled once to require 3x as many uses " +
                        "before it settles again")
                .defineInRange("settle_level_multiplier", 3d, Double.MIN_VALUE, Double.MAX_VALUE);

        settleLimitDurabilityMultiplier = builder
                .comment("Durability multiplier for settling limit, a value of 1 would cause a module with 75 durability to require an additional " +
                        "75 uses before it settles")
                .defineInRange("settle_durability_multiplier", 0.5d, Double.MIN_VALUE, Double.MAX_VALUE);

        honeSwordBase = builder
                .comment("The base value for number of uses required before a sword can be honed")
                .defineInRange("hone_sword_base", HONE_SWORD_BASE_DEFAULT, Integer.MIN_VALUE, Integer.MAX_VALUE);

        honeSwordIntegrityMultiplier = builder
                .comment("Integrity multiplier for sword honing, a value of 2 would cause a sword which uses 3 integrity to require 2*3 times as " +
                        "many uses before it can be honed")
                .defineInRange("hone_sword_integrity_multiplier", HONE_SWORD_INTEGRITY_MULTIPLIER_DEFAULT, Integer.MIN_VALUE, Integer.MAX_VALUE);

        honedoubleBase = builder
                .comment("The base value for number of uses required before a tool can be honed")
                .defineInRange("hone_double_base", HONE_DOUBLE_BASE_DEFAULT, Integer.MIN_VALUE, Integer.MAX_VALUE);

        honedoubleIntegrityMultiplier = builder
                .comment("Integrity multiplier for tool honing, a value of 2 would cause a sword which uses 3 integrity to require 2*3 times as " +
                        "many uses before it can be honed")
                .defineInRange("hone_double_integrity_multiplier", HONE_DOUBLE_INTEGRITY_MULTIPLIER_DEFAULT, Integer.MIN_VALUE, Integer.MAX_VALUE);

        honeBowBase = builder
                .comment("The base value for number of uses required before a bow can be honed")
                .defineInRange("hone_bow_base", HONE_BOW_BASE_DEFAULT, Integer.MIN_VALUE, Integer.MAX_VALUE);

        honeBowIntegrityMultiplier = builder
                .comment("Integrity multiplier for bow honing, a value of 2 would cause a bow which uses 3 integrity to require 2*3 times as many " +
                        "uses before it can be honed")
                .defineInRange("hone_bow_integrity_multiplier", HONE_BOW_INTEGRITY_MULTIPLIER_DEFAULT, Integer.MIN_VALUE, Integer.MAX_VALUE);

        honeShieldBase = builder
                .comment("The base value for number of uses required before a shield can be honed")
                .defineInRange("hone_shield_base", HONE_SHIELD_BASE_DEFAULT, Integer.MIN_VALUE, Integer.MAX_VALUE);

        honeShieldIntegrityMultiplier = builder
                .comment("Integrity multiplier for shield honing, a value of 2 would cause a shield which uses 3 integrity to require 2*3 times as " +
                        "many uses before it can be honed")
                .defineInRange("hone_shield_integrity_multiplier", HONE_SHIELD_INTEGRITY_MULTIPLIER_DEFAULT, Integer.MIN_VALUE, Integer.MAX_VALUE);

        honeCrossbowBase = builder
                .comment("The base value for number of uses required before a crossbow can be honed")
                .defineInRange("hone_crossbow_base", HONE_CROSSBOW_BASE_DEFAULT, Integer.MIN_VALUE, Integer.MAX_VALUE);

        honeCrossbowIntegrityMultiplier = builder
                .comment("Integrity multiplier for crossbow honing, a value of 2 would cause a crossbow which uses 3 integrity to require 2*3 times" +
                        " as many uses before it can be honed")
                .defineInRange("hone_crossbow_integrity_multiplier", HONE_CROSSBOW_INTEGRITY_MULTIPLIER_DEFAULT, Integer.MIN_VALUE, Integer.MAX_VALUE);

        honeSingleBase = builder
                .comment("The base value for number of uses required before a single headed implement can be honed")
                .defineInRange("hone_single_headed_base", HONE_SINGLE_BASE_DEFAULT, Integer.MIN_VALUE, Integer.MAX_VALUE);

        honeSingleIntegrityMultiplier = builder
                .comment("Integrity multiplier for single headed implement honing, a value of 2 would cause an implement which uses 3 integrity to " +
                        "require 2*3 times as many uses before it can be honed")
                .defineInRange("hone_single_headed_integrity_multiplier", HONE_SINGLE_INTEGRITY_MULTIPLIER_DEFAULT, Integer.MIN_VALUE, Integer.MAX_VALUE);

        builder.pop();

        // experimental config
        builder
                .comment("Toggles & config for experimental features")
                .push("experimental");

        enableExtractor = builder
                .comment("Enable the extractor bedrock functionality")
                .worldRestart()
                .define("extractor", true);

        enableCrossbow = builder
                .comment("Enable modular crossbows")
                .worldRestart()
                .define("crossbow", true);

        builder.pop();

        spec = builder.build();
    }

    public static void setup(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, spec, "tetra.toml");
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        onModConfigLoad();
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading configEvent) {
        onModConfigLoad();
    }


    private static void onModConfigLoad() {
        if (ModularBladedItem.instance != null) {
            ModularBladedItem.instance.updateConfig(honeSwordBase.get(), honeSwordIntegrityMultiplier.get());
        }

        if (ModularDoubleHeadedItem.instance != null) {
            ModularDoubleHeadedItem.instance.updateConfig(honedoubleBase.get(), honedoubleIntegrityMultiplier.get());
        }

        if (ModularBowItem.instance != null) {
            ModularBowItem.instance.updateConfig(honeBowBase.get(), honeBowIntegrityMultiplier.get());
        }

        if (ModularCrossbowItemImpl.instance != null) {
            ModularCrossbowItemImpl.instance.updateConfig(honeCrossbowBase.get(), honeCrossbowIntegrityMultiplier.get());
        }

        if (ModularSingleHeadedItem.instance != null) {
            ModularSingleHeadedItem.instance.updateConfig(honeSingleBase.get(), honeSingleIntegrityMultiplier.get());
        }

        if (ModularShieldItem.instance != null) {
            ModularShieldItem.instance.updateConfig(honeShieldBase.get(), honeShieldIntegrityMultiplier.get());
        }
    }
}
