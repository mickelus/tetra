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
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
//        setupEnchantment(Enchantments.MULTISHOT);
//        setupEnchantment(Enchantments.QUICK_CHARGE);
//        setupEnchantment(Enchantments.PIERCING);

        // fishing rods
//        setupEnchantment(Enchantments.LUCK_OF_THE_SEA);
//        setupEnchantment(Enchantments.LURE);
//
//        // apotheosis
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "capturing")));
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "hell_infusion")));
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "mounted_strike")));
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "depth_miner")));
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "stable_footing")));
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "scavenger")));
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "life_mending")));
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "icy_thorns")));
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "tempting")));
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "shield_bash")));
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "reflective")));
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "berserk")));
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "knowledge")));
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "splitting")));
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "natures_blessing")));
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "rebounding")));
//        setupEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("apotheosis", "magic_protection")));
//
//        // cyclic
//        setupEnchantment(EnchantRegistry.excavate);
//        setupEnchantment(EnchantRegistry.experience_boost);
//        setupEnchantment(EnchantRegistry.life_leech);
//        setupEnchantment(EnchantRegistry.launch);
//        setupEnchantment(EnchantRegistry.magnet);
//        setupEnchantment(EnchantRegistry.multishot);
//        setupEnchantment(EnchantRegistry.quickshot);
//        setupEnchantment(EnchantRegistry.reach);
//        setupEnchantment(EnchantRegistry.venom);
//
//        // natures aura
//        setupEnchantment(de.ellpeck.naturesaura.enchant.ModEnchantments.AURA_MENDING);

        // corail tombstone
//        setupEnchantment(ovh.corail.tombstone.registry.ModEnchantments.shadow_step);
//        setupEnchantment(ovh.corail.tombstone.registry.ModEnchantments.soulbound);
//        setupEnchantment(ovh.corail.tombstone.registry.ModEnchantments.magic_siphon);
//        setupEnchantment(ovh.corail.tombstone.registry.ModEnchantments.plague_bringer);
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
        Path outputPath = generator.getOutputFolder().resolve("TEMP/" + lang + ".json");
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
