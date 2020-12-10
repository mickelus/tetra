package se.mickelus.tetra.data.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class EnchantmentProvider implements IDataProvider {
    private static final Logger logger = LogManager.getLogger();

    public static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final DataGenerator generator;
    private final String lang = "en_us";

    private List<EnchantmentBuilder> builders;

    public EnchantmentProvider(DataGenerator generator, ExistingFileHelper exFileHelper) {
        this.generator = generator;

        builders = new LinkedList<>();
    }

    private void setup() {
        // generic
        setupEnchantment(Enchantments.MENDING);
        setupEnchantment(Enchantments.VANISHING_CURSE);

        // swords
        setupEnchantment(Enchantments.SHARPNESS)
                .setApply(false)
                .setCreateImprovements(false);
        setupEnchantment(Enchantments.SMITE);
        setupEnchantment(Enchantments.BANE_OF_ARTHROPODS);
        setupEnchantment(Enchantments.KNOCKBACK);
        setupEnchantment(Enchantments.FIRE_ASPECT);
        setupEnchantment(Enchantments.LOOTING);
        setupEnchantment(Enchantments.SWEEPING)
                .setApply(false)
                .setCreateImprovements(false);

        // tridents
        setupEnchantment(Enchantments.LOYALTY);
        setupEnchantment(Enchantments.IMPALING);
        setupEnchantment(Enchantments.RIPTIDE);
        setupEnchantment(Enchantments.CHANNELING);

        // tools
        setupEnchantment(Enchantments.EFFICIENCY)
                .setApply(false)
                .setCreateImprovements(false);
        setupEnchantment(Enchantments.SILK_TOUCH);
        setupEnchantment(Enchantments.UNBREAKING)
                .setApply(false)
                .setCreateImprovements(false);
        setupEnchantment(Enchantments.FORTUNE);

        // bows
        setupEnchantment(Enchantments.POWER);
        setupEnchantment(Enchantments.PUNCH);
        setupEnchantment(Enchantments.FLAME);
        setupEnchantment(Enchantments.INFINITY);

        // armor
//        setupEnchantment(Enchantments.PROTECTION);
//        setupEnchantment(Enchantments.FIRE_PROTECTION);
//        setupEnchantment(Enchantments.FEATHER_FALLING);
//        setupEnchantment(Enchantments.BLAST_PROTECTION);
//        setupEnchantment(Enchantments.PROJECTILE_PROTECTION);
//        setupEnchantment(Enchantments.RESPIRATION);
//        setupEnchantment(Enchantments.AQUA_AFFINITY);
//        setupEnchantment(Enchantments.THORNS);
//        setupEnchantment(Enchantments.DEPTH_STRIDER);
//        setupEnchantment(Enchantments.FROST_WALKER);
//        setupEnchantment(Enchantments.BINDING_CURSE);

        // crossbows
        setupEnchantment(Enchantments.MULTISHOT);
        setupEnchantment(Enchantments.QUICK_CHARGE);
        setupEnchantment(Enchantments.PIERCING);

        // fishing rods
//        setupEnchantment(Enchantments.LUCK_OF_THE_SEA);
//        setupEnchantment(Enchantments.LURE);

        // apotheosis
        setupEnchantment("apotheosis:capturing");
        setupEnchantment("apotheosis:hell_infusion");
        setupEnchantment("apotheosis:mounted_strike");
        setupEnchantment("apotheosis:depth_miner");
        setupEnchantment("apotheosis:stable_footing");
        setupEnchantment("apotheosis:scavenger");
        setupEnchantment("apotheosis:life_mending");
        setupEnchantment("apotheosis:icy_thorns");
        setupEnchantment("apotheosis:tempting");
        setupEnchantment("apotheosis:shield_bash");
        setupEnchantment("apotheosis:reflective");
        setupEnchantment("apotheosis:berserk");
        setupEnchantment("apotheosis:knowledge");
        setupEnchantment("apotheosis:splitting");
        setupEnchantment("apotheosis:natures_blessing");
        setupEnchantment("apotheosis:rebounding");
        setupEnchantment("apotheosis:magic_protection");

        // cyclic
        setupEnchantment("cyclic:excavate");
        setupEnchantment("cyclic:experience_boost");
        setupEnchantment("cyclic:life_leech");
        setupEnchantment("cyclic:launch");
        setupEnchantment("cyclic:magnet");
        setupEnchantment("cyclic:multishot");
        setupEnchantment("cyclic:quickshot");
        setupEnchantment("cyclic:reach");
        setupEnchantment("cyclic:venom");
        setupEnchantment("cyclic:beheading");
        setupEnchantment("cyclic:step");

        // natures aura
        setupEnchantment("naturesaura:aura_mending");

        // ensorcellation
        setupEnchantment("ensorcellation:magic_protection");
        setupEnchantment("ensorcellation:displacement");
        setupEnchantment("ensorcellation:fire_rebuke");
        setupEnchantment("ensorcellation:frost_rebuke");
        setupEnchantment("ensorcellation:air_affinity");
        setupEnchantment("ensorcellation:exp_boost");
        setupEnchantment("ensorcellation:gourmand");
        setupEnchantment("ensorcellation:reach");
        setupEnchantment("ensorcellation:vitality");
        setupEnchantment("ensorcellation:damage_ender");
        setupEnchantment("ensorcellation:damage_illager");
        setupEnchantment("ensorcellation:damage_villager");
        setupEnchantment("ensorcellation:cavalier");
        setupEnchantment("ensorcellation:frost_aspect");
        setupEnchantment("ensorcellation:leech");
        setupEnchantment("ensorcellation:magic_edge");
        setupEnchantment("ensorcellation:vorpal");
        setupEnchantment("ensorcellation:excavating");
        setupEnchantment("ensorcellation:hunter");
        setupEnchantment("ensorcellation:quick_draw");
        setupEnchantment("ensorcellation:trueshot");
        setupEnchantment("ensorcellation:volley");
        setupEnchantment("ensorcellation:angler");
        setupEnchantment("ensorcellation:pilfering");
        setupEnchantment("ensorcellation:furrowing");
        setupEnchantment("ensorcellation:tilling");
        setupEnchantment("ensorcellation:weeding");
        setupEnchantment("ensorcellation:bulwark");
        setupEnchantment("ensorcellation:phalanx");
        setupEnchantment("ensorcellation:soulbound");
        setupEnchantment("ensorcellation:curse_fool");
        setupEnchantment("ensorcellation:curse_mercy");

        // bluepower
        setupEnchantment("bluepower:vorpal");
        setupEnchantment("bluepower:disjunction");

        // enchantable
        setupEnchantment("enchantable:stomping");
        setupEnchantment("enchantable:iron_skull");
        setupEnchantment("enchantable:replanting");
        setupEnchantment("enchantable:cultivator");
        setupEnchantment("enchantable:excavator");
        setupEnchantment("enchantable:ore_eater");

        // ma enchants
        setupEnchantment("ma-enchants:curse_breaking");
        setupEnchantment("ma-enchants:curse_butterfingers");
        setupEnchantment("ma-enchants:curse_aquaphobia");
        setupEnchantment("ma-enchants:curse_death");
        setupEnchantment("ma-enchants:reinforced_tip");
        setupEnchantment("ma-enchants:stone_mending");
        setupEnchantment("ma-enchants:lumberjack");
        setupEnchantment("ma-enchants:momentum");
        setupEnchantment("ma-enchants:butchering");
        setupEnchantment("ma-enchants:true_shot");
        setupEnchantment("ma-enchants:quick_draw");
        setupEnchantment("ma-enchants:floating");
        setupEnchantment("ma-enchants:paralysis");
        setupEnchantment("ma-enchants:detonation");
        setupEnchantment("ma-enchants:combo");
        setupEnchantment("ma-enchants:faster_attack");
        setupEnchantment("ma-enchants:lifesteal");
        setupEnchantment("ma-enchants:ice_aspect");
        setupEnchantment("ma-enchants:wisdom");
        setupEnchantment("ma-enchants:blazing_walker");
        setupEnchantment("ma-enchants:step_assist");
        setupEnchantment("ma-enchants:night_vision");
        setupEnchantment("ma-enchants:multi_jump");
        setupEnchantment("ma-enchants:timeless");

        setupEnchantment("cursed:blindness");
        setupEnchantment("cursed:curtail");
        setupEnchantment("cursed:echo");
        setupEnchantment("cursed:encumbrance");
        setupEnchantment("cursed:fading");
        setupEnchantment("cursed:fragility");
        setupEnchantment("cursed:ignorance");
        setupEnchantment("cursed:insomnia");
        setupEnchantment("cursed:misfortune");
        setupEnchantment("cursed:obedience");
        setupEnchantment("cursed:radiance");
        setupEnchantment("cursed:silence");
        setupEnchantment("cursed:sinking");
    }

    @Override
    public void act(DirectoryCache cache) throws IOException {
        setup();
        JsonObject localization = new JsonObject();
        JsonObject missingLocalization = new JsonObject();

        // group builders by mod
        builders.stream()
                .collect(Collectors.groupingBy(EnchantmentBuilder::getModId))
                .forEach((mod, builders) -> {
                    JsonArray enchantments = new JsonArray();
                    JsonArray destabilization = new JsonArray();

                    // enchantment improvements
                    for (EnchantmentBuilder builder: builders) {
                        enchantments.add(builder.getEnchantmentJson());
                        if (builder.canDestabilize()) {
                            enchantments.add(builder.getEnchantmentDestabilizationJson());
                            destabilization.add(builder.getDestabilizationJson());
                        } else if (builder.isDestabilizingCurse()) {
                            destabilization.add(builder.getDestabilizationJson());
                        }

                        if (builder.shouldCreateImprovements()) {
                            saveImprovements(cache, builder.getKey(), builder.getImprovementsJson());
                        }
                    }

                    // english localization for names and descriptions
                    URL locFile = getClass().getClassLoader().getResource(String.format("assets/%s/lang/%s.json", mod, lang));
                    if (locFile != null) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(locFile.openStream()))) {
                            JsonObject existingLocalization = gson.fromJson(reader, JsonObject.class);
                            builders.stream()
                                    .map(builder -> builder.getLocalizationEntries(existingLocalization))
                                    .map(Map::entrySet)
                                    .flatMap(Set::stream)
                                    .forEach(entry -> {
                                        if (!entry.getValue().isEmpty()) {
                                            localization.addProperty(entry.getKey(), entry.getValue());
                                        } else {
                                            missingLocalization.addProperty(entry.getKey(), entry.getValue());
                                        }
                                    });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        builders.stream()
                                .map(EnchantmentBuilder::getLocalizationKeys)
                                .flatMap(Collection::stream)
                                .forEach(key -> missingLocalization.addProperty(key, ""));
                    }

                    saveEnchantments(cache, mod, enchantments);
                    saveDestabilization(cache, mod, destabilization);
                });

        saveLocalization(cache, localization);
        saveMissingLocalization(cache, missingLocalization);

        // enchantment type mappings
        JsonObject typeMappings = new JsonObject();
        builders.stream()
                .collect(Collectors.groupingBy(EnchantmentBuilder::getEnchantmentType))
                .forEach((enchantmentType, builders) -> {
                    JsonArray enchantments = new JsonArray();
                    builders.stream()
                            .map(EnchantmentBuilder::getKey)
                            .map(key -> "tetra:" + key)
                            .forEach(enchantments::add);

                    typeMappings.add(enchantmentType.toString(), enchantments);
                });

        saveImprovementTypeMappings(cache, typeMappings);
    }

    private EnchantmentBuilder setupEnchantment(String enchantmentId) {
        Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(enchantmentId));

        if (enchantment == null) {
            throw new NullPointerException("Missing enchantment '" + enchantmentId + "'");
        }

        EnchantmentBuilder builder = new EnchantmentBuilder(enchantment);
        builders.add(builder);
        return builder;
    }

    private EnchantmentBuilder setupEnchantment(Enchantment enchantment) {
        EnchantmentBuilder builder = new EnchantmentBuilder(enchantment);
        builders.add(builder);
        return builder;
    }

    private void saveEnchantments(DirectoryCache cache, String modId, JsonArray enchantments) {
        Path outputPath = generator.getOutputFolder().resolve("data/tetra/enchantments/" + modId + ".json");
        try {
            IDataProvider.save(gson, cache, enchantments, outputPath);
        } catch (IOException e) {
            logger.error("Couldn't save enchantments to {}", outputPath, e);
        }
    }

    private void saveDestabilization(DirectoryCache cache, String modId, JsonArray destabilization) {
        Path outputPath = generator.getOutputFolder().resolve("data/tetra/destabilization/" + modId + ".json");
        try {
            IDataProvider.save(gson, cache, destabilization, outputPath);
        } catch (IOException e) {
            logger.error("Couldn't save destabilization to {}", outputPath, e);
        }
    }

    private void saveImprovements(DirectoryCache cache, String enchantmentKey, JsonArray improvements) {
        Path outputPath = generator.getOutputFolder().resolve("data/tetra/improvements/" + enchantmentKey + ".json");
        try {
            IDataProvider.save(gson, cache, improvements, outputPath);
        } catch (IOException e) {
            logger.error("Couldn't save improvements to {}", outputPath, e);
        }
    }

    private void saveLocalization(DirectoryCache cache, JsonObject localizationEntries) {
        Path outputPath = generator.getOutputFolder().resolve("temp/enchantments_" + lang + ".json");
        try {
            IDataProvider.save(gson, cache, localizationEntries, outputPath);
        } catch (IOException e) {
            logger.error("Couldn't save localization to {}", outputPath, e);
        }
    }

    private void saveMissingLocalization(DirectoryCache cache, JsonObject localizationEntries) {
        Path outputPath = generator.getOutputFolder().resolve("temp/missing_localization.json");
        try {
            IDataProvider.save(gson, cache, localizationEntries, outputPath);
        } catch (IOException e) {
            logger.error("Couldn't save missing localization to {}", outputPath, e);
        }
    }

    private void saveImprovementTypeMappings(DirectoryCache cache, JsonObject mappings) {
        Path outputPath = generator.getOutputFolder().resolve("temp/enchantment_mappings.json");
        try {
            IDataProvider.save(gson, cache, mappings, outputPath);
        } catch (IOException e) {
            logger.error("Couldn't save missing localization to {}", outputPath, e);
        }
    }

    @Override
    public String getName() {
        return "tetra enchantment data provider";
    }
}
