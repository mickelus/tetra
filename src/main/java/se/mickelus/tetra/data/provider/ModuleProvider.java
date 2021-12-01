package se.mickelus.tetra.data.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.ToolTypes;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
@ParametersAreNonnullByDefault
public class ModuleProvider implements DataProvider {
    private static final Logger logger = LogManager.getLogger();

    public static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final DataGenerator generator;

    private List<ModuleBuilder> builders;

    private ExistingFileHelper existingFileHelper;

    private final String lang = "en_us";

    public ModuleProvider(DataGenerator generator, ExistingFileHelper exFileHelper) {
        this.generator = generator;
        this.existingFileHelper = exFileHelper;

        builders = new LinkedList<>();

    }

    /**
     * This provider can be used to generate additional variants for existing modules, useful for mod devs or pack makers that want to make
     * tetra modules compatible with materials from other mods
     */
    @SuppressWarnings("unchecked")
    private void setup() {
        // the generator uses an existing module variant as a template when generating data, these are matched against the variant key to find a fitting template
        // it matches against them in decending order
        String[] metalReferences = new String[] { "iron" };
        String[] woodReferences = new String[] { "oak", "wooden" };
        String[] gemReferences = new String[] { "diamond" };

        // data shared between all module variants of the same materials are defined here, e.g. the localized
        ModuleBuilder.Material platinum = new ModuleBuilder.Material("platinum", "platinum", 0xd6f6ff, 0xd6f6ff,
                1, 66, "tag", "forge:ingots/platinum", 1, ToolTypes.hammer, 4, metalReferences);

        ModuleBuilder.Material ruby = new ModuleBuilder.Material("ruby", "ruby", 0xa349f2, 0xa349f2,
                1, 60, "tag", "forge:gems/ruby", 1, ToolTypes.hammer, 2, gemReferences, Pair.of("arrested", 0));

        ModuleBuilder.Material enigmaticIron = new ModuleBuilder.Material("my_metal", "mystic metal", 0xddeeee, 0xeeaabb,
                2, 88, "item", "mymod:my_metal", 1, ToolTypes.hammer, 1, metalReferences);


        // Setup each module which should have additional variants generated here, using the materials defined above. The material is paired with an
        // an item below which is then used to grab additional data for the module, e.g. damage, mining speed or durability. There are several methods
        // for offsetting the values that are grabbed from both the item and the material
        setupModule(
                "double/basic_pickaxe", // this is resource location / path for the module, check src/main/resources/data/tetra/modules to see what's available
                "basic_pickaxe", // this will be used to prefix variant keys, variant keys typically begin with the module name e.g. basic_pickaxe/iron
                "%s pick", // %s will be replaced by the localization entry for each material to produce the names for all module variants
                "basic_pickaxe/iron", // the generator will fall back to using the variant with this key if none of the references from the material matches any variant key
                "double/basic_pickaxe/basic_pickaxe") // the path for the schematic file, I've not been consistent in how I've structured this so double check that this is correct
                .offsetDurability(-20, 0.5f) // pickaxes have two heads and the default handle has 20 durability so the durability of the module should be = (itemDurability - 20) * 0.5
                .offsetSpeed(0, 0.5f) // same math goes for the speed, the flimsy handle has no impact on speed so the speed of the item should be split equally between the heads
                .addVariant(platinum, "minecraft:iron_pickaxe")
                .addVariant(ruby, "minecraft:diamond_pickaxe")
                .addVariant(enigmaticIron, Pair.of("enchantment/fortune", 1));

        setupModule("double/basic_axe", "basic_axe", "%s axe", "basic_axe/iron", "double/basic_axe/basic_axe")
                .offsetOutcome(2, 0) // offsets the amount of material required (defined per material above) by a multiplier of two
                .offsetDurability(-20, 0.7f)
                .offsetSpeed(-0.1f, 1)
                .addVariant(platinum, "minecraft:iron_axe")
                .addVariant(ruby, "minecraft:diamond_axe", Pair.of("enchantment/fire_aspect", 1))
                .addVariant(enigmaticIron, Pair.of("enchantment/looting", 1));

        setupModule("double/butt", "butt", "%s butt", "butt/iron", "double/butt/butt")
                .offsetOutcome(1, -1)
                .addVariant(platinum)
                .addVariant(ruby)
                .addVariant(enigmaticIron);
    }

    @Override
    public void run(HashCache cache) throws IOException {
        setup();

        builders.forEach(builder -> saveModule(cache, builder.module, builder.getModuleJson()));
        builders.forEach(builder -> saveSchematic(cache, builder.schematicPath, builder.getSchematicJson()));

        JsonObject localization = new JsonObject();
        builders.stream()
                .map(ModuleBuilder::getLocalizationEntries)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .forEach(entry -> localization.addProperty(entry.getKey(), entry.getValue()));
        saveLocalization(cache, localization);
    }

    private ModuleBuilder setupModule(String module, String prefix, String localization, String fallbackReference, String schematicPath) {
        JsonObject referenceModule = null;
        try {
            Resource resource = existingFileHelper.getResource(new ResourceLocation("tetra", module), PackType.SERVER_DATA, ".json", "modules");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            referenceModule = gson.fromJson(reader, JsonObject.class);

        } catch (IOException e) {
            e.printStackTrace();
        }


        ModuleBuilder builder = new ModuleBuilder(module, prefix, localization, referenceModule, fallbackReference, schematicPath);
        builders.add(builder);
        return builder;
    }

    private void saveModule(HashCache cache, String moduleKey, JsonObject moduleData) {
        Path outputPath = generator.getOutputFolder().resolve("data/tetra/modules/" + moduleKey + ".json");
        try {
            DataProvider.save(gson, cache, moduleData, outputPath);
        } catch (IOException e) {
            logger.error("Couldn't save module data to {}", outputPath, e);
        }
    }

    private void saveSchematic(HashCache cache, String schematicPath, JsonObject schematicData) {
        Path outputPath = generator.getOutputFolder().resolve("data/tetra/schematics/" + schematicPath + ".json");
        try {
            DataProvider.save(gson, cache, schematicData, outputPath);
        } catch (IOException e) {
            logger.error("Couldn't save schematic data to {}", outputPath, e);
        }
    }

    private void saveLocalization(HashCache cache, JsonObject localizationEntries) {
        Path outputPath = generator.getOutputFolder().resolve("temp/modules_" + lang + ".json");
        try {
            DataProvider.save(gson, cache, localizationEntries, outputPath);
        } catch (IOException e) {
            logger.error("Couldn't save localization to {}", outputPath, e);
        }
    }

    @Override
    public String getName() {
        return "tetra module data provider";
    }
}
