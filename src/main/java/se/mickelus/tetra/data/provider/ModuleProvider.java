package se.mickelus.tetra.data.provider;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.resources.IResource;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.capabilities.Capability;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;

public class ModuleProvider implements IDataProvider {
    private static final Logger logger = LogManager.getLogger();

    public static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final DataGenerator generator;

    private List<ModuleBuilder> builders;

    private ExistingFileHelper existingFileHelper;

    public ModuleProvider(DataGenerator generator, ExistingFileHelper exFileHelper) {
        this.generator = generator;
        this.existingFileHelper = exFileHelper;

        builders = new LinkedList<>();

    }

    @SuppressWarnings("unchecked")
    private void setup() {
        ModuleBuilder.Material platinum = new ModuleBuilder.Material("platinum", 0xd6f6ff, 0xd6f6ff, 1, 66, "tag", "forge:ingots/platinum", 1, Capability.hammer, 4);
        ModuleBuilder.Material ruby = new ModuleBuilder.Material("ruby", 0xa349f2, 0xa349f2, 1, 60, "tag", "forge:gems/ruby", 1, Capability.hammer, 2, Pair.of("arrested", 0));
        ModuleBuilder.Material enigmaticIron = new ModuleBuilder.Material("enigmatic_iron", 0xddeeee, 0xeeaabb, 2, 55);

        setupModule("double/basic_pickaxe", "basic_pickaxe", "basic_pickaxe/iron", "double/basic_pickaxe/basic_pickaxe")
                .offsetDurability(-20, 0.5f)
                .offsetSpeed(0, 0.5f)
                .addVariant(platinum, "minecraft:iron_pickaxe")
                .addVariant(ruby, "minecraft:diamond_pickaxe")
                .addVariant(enigmaticIron);

        setupModule("double/basic_axe", "basic_axe", "basic_axe/iron", "double/basic_axe/basic_axe")
                .offsetOutcome(2, 0)
                .offsetDurability(-20, 0.7f)
                .offsetSpeed(-0.1f, 1)
                .addVariant(platinum, "minecraft:iron_axe")
                .addVariant(ruby, "minecraft:diamond_axe", Pair.of("enchantment/fire_aspect", 1))
                .addVariant(enigmaticIron);

        setupModule("double/butt", "butt", "butt/iron", "double/butt/butt")
                .offsetOutcome(1, -1)
                .addVariant(platinum)
                .addVariant(ruby)
                .addVariant(enigmaticIron);
    }

    @Override
    public void act(DirectoryCache cache) throws IOException {
        setup();

        builders.forEach(builder -> saveModule(cache, builder.module, builder.getModuleJson()));
        builders.forEach(builder -> saveSchema(cache, builder.schemaPath, builder.getSchemaJson()));
    }

    private ModuleBuilder setupModule(String module, String prefix, String referenceVariant, String schemaPath) {
        JsonObject referenceModule = null;
        try {
            IResource resource = existingFileHelper.getResource(new ResourceLocation("tetra", module), ResourcePackType.SERVER_DATA, ".json", "modules");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            referenceModule = gson.fromJson(reader, JsonObject.class);

        } catch (IOException e) {
            e.printStackTrace();
        }


        ModuleBuilder builder = new ModuleBuilder(module, prefix, getVariantJson(referenceModule, referenceVariant), schemaPath);
        builders.add(builder);
        return builder;
    }

    private JsonObject getVariantJson(JsonObject moduleJson, String referenceVariant) {
        JsonArray variants = moduleJson.getAsJsonArray("variants");

        for (int i = 0; i < variants.size(); i++) {
            JsonObject variant = variants.get(i).getAsJsonObject();
            if (referenceVariant.equals(variant.get("key").getAsString())) {
                return variant;
            }
        }

        throw new NullPointerException("Could not find module variant: " + referenceVariant);
    }

    private void saveModule(DirectoryCache cache, String moduleKey, JsonObject moduleData) {
        Path outputPath = generator.getOutputFolder().resolve("data/tetra/modules/" + moduleKey + ".json");
        try {
            IDataProvider.save(gson, cache, moduleData, outputPath);
        } catch (IOException e) {
            logger.error("Couldn't save module data to {}", outputPath, e);
        }
    }

    private void saveSchema(DirectoryCache cache, String schemaPath, JsonObject schemaData) {
        Path outputPath = generator.getOutputFolder().resolve("data/tetra/schema/" + schemaPath + ".json");
        try {
            IDataProvider.save(gson, cache, schemaData, outputPath);
        } catch (IOException e) {
            logger.error("Couldn't save schema data to {}", outputPath, e);
        }
    }

    @Override
    public String getName() {
        return "tetra module data provider";
    }
}
