package se.mickelus.tetra.data.provider;

import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@ParametersAreNonnullByDefault
public class ModuleBuilder {

    public String module;
    public String prefix;

    public String localization;

    public String schematicPath;

    public JsonObject referenceModule;
    public String fallbackReference;

    private int durabilityOffset = 0;
    private float durabilityMultiplier = 1;

    private float speedOffset = 0;
    private float speedMultiplier = 1;

    private int integrityOffset = 0;

    private float countMultiplier = 1;
    private int toolOffset = 0;

    private ArrayList<Variant> variants = new ArrayList<>();

    private Map<ToolAction, BlockState> harvestMap;

    public ModuleBuilder(String module, String prefix, String localization, JsonObject referenceModule, String fallbackReference, String schematicPath) {
        this.module = module;
        this.prefix = prefix;

        this.localization = localization;

        this.schematicPath = schematicPath;

        this.referenceModule = referenceModule;
        this.fallbackReference = fallbackReference;

        harvestMap = new HashMap<>();
        harvestMap.put(ToolActions.AXE_DIG, Blocks.OAK_LOG.defaultBlockState());
        harvestMap.put(ToolActions.PICKAXE_DIG, Blocks.STONE.defaultBlockState());
        harvestMap.put(ToolActions.SHOVEL_DIG, Blocks.DIRT.defaultBlockState());
    }

    public ModuleBuilder offsetOutcome(int countMultiplier, int toolOffset) {
        this.countMultiplier = countMultiplier;
        this.toolOffset = toolOffset;

        return this;
    }

    public ModuleBuilder offsetDurability(int flat, float multiplier) {
        durabilityOffset = flat;
        durabilityMultiplier = multiplier;
        return this;
    }

    public ModuleBuilder offsetSpeed(float flat, float multiplier) {
        speedOffset = flat;
        speedMultiplier = multiplier;

        return this;
    }

    public ModuleBuilder offsetIntegrity(int integrity) {
        this.integrityOffset = integrity;
        return this;
    }

    public ModuleBuilder addVariant(Material material) {
        variants.add(new Variant(material, null));
        return this;
    }

    public ModuleBuilder addVariant(Material material, Pair<String, Integer> ... improvements) {
        variants.add(new Variant(material, null, improvements));
        return this;
    }

    public ModuleBuilder addVariant(Material material, String itemId) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));

        if (item == null) {
            throw new NullPointerException("Missing item '" + itemId + "'");
        }
        variants.add(new Variant(material, item));

        return this;
    }

    public ModuleBuilder addVariant(Material material, String itemId, Pair<String, Integer> ... improvements) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));

        if (item == null) {
            throw new NullPointerException("Missing item '" + itemId + "'");
        }
        variants.add(new Variant(material, item, improvements));

        return this;
    }

    private JsonObject getReferenceVariant(JsonObject moduleJson, String[] variantReferences, String fallbackReference) {
        JsonArray variants = moduleJson.getAsJsonArray("variants");

        String[] referenceKeys = ArrayUtils.add(variantReferences, fallbackReference);

        for (int i = 0; i < referenceKeys.length; i++) {
            for (int j = 0; j < variants.size(); j++) {
                JsonObject variant = variants.get(j).getAsJsonObject();
                String variantKey = variant.get("key").getAsString();
                if (variantKey.contains(referenceKeys[i])) {
                    return variant;
                }
            }
        }

        throw new NullPointerException("Could not find variant reference for: " + module);
    }

    public JsonObject getModuleJson() {
        JsonObject result = new JsonObject();
        JsonArray variantsJson = new JsonArray();

        variants.stream()
                .map(this::getVariantJson)
                .forEach(variantsJson::add);

        result.add("variants", variantsJson);

        return result;
    }

    private JsonObject getVariantJson(Variant variant) {
        Material material = variant.material;
        Item item = variant.item;

        JsonObject referenceVariant = getReferenceVariant(referenceModule, variant.material.references, fallbackReference);
        JsonObject result = deepCopy(referenceVariant);

        result.addProperty("key", prefix + "/" + material.key);

        result.getAsJsonObject("glyph").addProperty("tint", Integer.toHexString(material.tint));
        result.getAsJsonArray("models").forEach(model -> model.getAsJsonObject().addProperty("tint", Integer.toHexString(material.materialTint)));

        result.addProperty("magicCapacity", material.magicCapacity);
        result.addProperty("integrity", integrityOffset > 0 ? integrityOffset + material.integrity : integrityOffset - material.integrity);

        if (item != null) {
            ItemStack itemStack = new ItemStack(item);
            Multimap<Attribute, AttributeModifier> attributes = itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND);

            if (result.has("durability")) {
                result.addProperty("durability", (int) (( itemStack.getMaxDamage() + durabilityOffset) * durabilityMultiplier));
            }

            if (result.has("damage")) {
                double damage = attributes.get(Attributes.ATTACK_DAMAGE).stream()
                        .filter(modifier -> AttributeModifier.Operation.ADDITION.equals(modifier.getOperation()))
                        .mapToDouble(AttributeModifier::getAmount)
                        .sum();
                result.addProperty("damage", damage);
            }

            double attackSpeed = attributes.get(Attributes.ATTACK_SPEED).stream()
                    .filter(modifier -> AttributeModifier.Operation.ADDITION.equals(modifier.getOperation()))
                    .mapToDouble(AttributeModifier::getAmount)
                    .sum();

            if (result.has("attackSpeed")) {
                result.addProperty("attackSpeed", ( attackSpeed + 2.4 + speedOffset ) * speedMultiplier);
            }

            if (result.has("tools")) {
                JsonObject toolsJson = result.getAsJsonObject("tools");
                Set<ToolAction> toolTypes = itemStack.getToolTypes();

                if (toolsJson.size() == toolTypes.size()) {
                    toolTypes.forEach(toolType -> {
                        BlockState blockState = harvestMap.get(toolType);

                        JsonArray value = new JsonArray();
                        value.add(itemStack.getHarvestLevel(toolType, null, blockState));
                        value.add(blockState != null ? item.getDestroySpeed(itemStack, blockState) / ( attackSpeed + 4 ): 0);

                        toolsJson.add(toolType.name(), value);
                    });
                } else {
                    long averageLevel = Math.round(toolTypes.stream()
                            .mapToInt(toolType -> itemStack.getHarvestLevel(toolType, null, harvestMap.get(toolType)))
                            .average()
                            .orElse(0));

                    double averageEfficiency = toolTypes.stream()
                            .mapToDouble(toolType -> item.getDestroySpeed(itemStack, harvestMap.get(toolType)))
                            .average()
                            .orElse(0) / ( attackSpeed+ 4 );

                    JsonArray value = new JsonArray();
                    value.add(averageLevel);
                    value.add(averageEfficiency);

                    for (Map.Entry<String, JsonElement> entry : toolsJson.entrySet()) {
                        toolsJson.add(entry.getKey(), value);
                    }
                }
            }
        }

        return result;
    }

    public JsonObject getSchematicJson() {
        JsonObject result = new JsonObject();
        JsonArray outcomesJson = new JsonArray();

        variants.stream()
                .map(this::getOutcomeJson)
                .forEach(outcomesJson::add);

        result.add("outcomes", outcomesJson);

        return result;
    }

    private JsonObject getOutcomeJson(Variant variant) {
        Material material = variant.material;

        JsonObject outcome = new JsonObject();

        JsonObject outcomeMaterial = new JsonObject();
        outcome.add("material", outcomeMaterial);
        outcomeMaterial.addProperty(material.type, material.itemId);

        if (Mth.ceil(material.count * countMultiplier) > 1) {
            outcomeMaterial.addProperty("count", Mth.ceil(material.count * countMultiplier));
        }


        if (material.toolType != null && material.toolLevel + toolOffset > 0) {
            JsonObject requiredTools = new JsonObject();
            outcome.add("requiredTools", requiredTools);
            requiredTools.addProperty(material.toolType.toString(), material.toolLevel + toolOffset);
        }

        outcome.addProperty("moduleKey", module);
        outcome.addProperty("moduleVariant", prefix + "/" + material.key);

        Map<String, Integer> improvementMap = Stream.concat(Arrays.stream(material.improvements), Arrays.stream(variant.improvements))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

        if (!improvementMap.isEmpty()) {
            JsonObject improvements = new JsonObject();
            outcome.add("improvements", improvements);

            for (Map.Entry<String, Integer> improvement: improvementMap.entrySet()) {
                improvements.addProperty(improvement.getKey(), improvement.getValue());
            }
        }

        return outcome;
    }

    public Map<String, String> getLocalizationEntries() {
        Map<String, String> result = new LinkedHashMap<>();

        variants.forEach(variant -> {
            result.put("tetra.variant." + prefix + "/" + variant.material.key, StringUtils.capitalize(String.format(localization, variant.material.localization)));
            result.put("tetra.variant." + prefix + "/" + variant.material.key + ".prefix", StringUtils.capitalize(variant.material.localization));
        });

        return result;
    }

    private JsonObject deepCopy(JsonObject object) {
        try {
            return ModuleProvider.gson.fromJson(ModuleProvider.gson.toJson(object), JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Material {
        String key;

        String localization;

        /** basically a hex color code (but with a prefix for java to parse it as an int, example: 0x00ff00 would be green **/
        int tint = 0x000000;

        int materialTint;

        /** this is relative the reference item **/
        int integrity;

        int magicCapacity;

        String type;
        String itemId;
        int count;
        ToolAction toolType;
        int toolLevel;
        Pair<String, Integer>[] improvements = new Pair[0];

        String[] references = new String[0];

        public Material(String key, String localization, int tint, int materialTint, int integrity, int magicCapacity, String type,
                String itemId, int count, ToolAction toolType, int toolLevel, String[] references) {
            this.key = key;
            this.localization = localization;
            this.tint = tint;
            this.materialTint = materialTint;
            this.integrity = integrity;
            this.magicCapacity = magicCapacity;

            this.type = type;
            this.itemId = itemId;
            this.count = count;
            this.toolType = toolType;
            this.toolLevel = toolLevel;

            this.references = references;
        }

        public Material(String key, String localization, int tint, int materialTint, int integrity, int magicCapacity, String type,
                String itemId, int count, ToolAction toolType, int toolLevel, String[] references, Pair<String, Integer> ... improvements) {
            this(key, localization, tint, materialTint, integrity, magicCapacity, type, itemId, count, toolType, toolLevel, references);

            this.improvements = improvements;
        }


    }

    public static class Variant {
        Material material;
        Item item;
        Pair<String, Integer>[] improvements = new Pair[0];

        public Variant(Material material, Item item) {
            this.material = material;
            this.item = item;
        }

        public Variant(Material material, Item item, Pair<String, Integer>[] improvements) {
            this.material = material;
            this.item = item;
            this.improvements = improvements;
        }
    }
}

