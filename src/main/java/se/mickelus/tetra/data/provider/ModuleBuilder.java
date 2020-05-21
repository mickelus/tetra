package se.mickelus.tetra.data.provider;

import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.tetra.capabilities.Capability;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModuleBuilder {

    public String module;
    public String prefix;

    public String schemaPath;

    public JsonObject referenceVariant;

    private int durabilityOffset = 0;
    private float durabilityMultiplier = 1;

    private float speedOffset = 0;
    private float speedMultiplier = 1;

    private int integrityOffset = 0;

    private float countMultiplier = 1;
    private int capabilityOffset = 0;

    private ArrayList<Variant> variants = new ArrayList<>();

    private Map<ToolType, BlockState> harvestMap;

    public ModuleBuilder(String module, String prefix, JsonObject referenceVariant, String schemaPath) {
        this.module = module;
        this.prefix = prefix;

        this.schemaPath = schemaPath;

        this.referenceVariant = referenceVariant;

        harvestMap = new HashMap<>();
        harvestMap.put(ToolType.AXE, Blocks.OAK_LOG.getDefaultState());
        harvestMap.put(ToolType.PICKAXE, Blocks.STONE.getDefaultState());
        harvestMap.put(ToolType.SHOVEL, Blocks.DIRT.getDefaultState());
    }

    public ModuleBuilder offsetOutcome(int countMultiplier, int capabilityOffset) {
        this.countMultiplier = countMultiplier;
        this.capabilityOffset = capabilityOffset;

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

        JsonObject result = deepCopy(referenceVariant);

        result.addProperty("key", prefix + "/" + material.key);

        result.getAsJsonObject("glyph").addProperty("tint", Integer.toHexString(material.tint));
        result.getAsJsonArray("models").forEach(model -> model.getAsJsonObject().addProperty("tint", Integer.toHexString(material.materialTint)));

        result.addProperty("magicCapacity", material.magicCapacity);
        result.addProperty("integrity", integrityOffset > 0 ? integrityOffset + material.integrity : integrityOffset - material.integrity);

        if (item != null) {
            ItemStack itemStack = new ItemStack(item);
            Multimap<String, AttributeModifier> attributes = itemStack.getAttributeModifiers(EquipmentSlotType.MAINHAND);

            if (result.has("durability")) {
                result.addProperty("durability", (int) (( itemStack.getMaxDamage() + durabilityOffset) * durabilityMultiplier));
            }

            if (result.has("damage")) {
                double damage = attributes.get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()).stream()
                        .filter(modifier -> AttributeModifier.Operation.ADDITION.equals(modifier.getOperation()))
                        .mapToDouble(AttributeModifier::getAmount)
                        .sum();
                result.addProperty("damage", damage);
            }

            double attackSpeed = attributes.get(SharedMonsterAttributes.ATTACK_SPEED.getName()).stream()
                    .filter(modifier -> AttributeModifier.Operation.ADDITION.equals(modifier.getOperation()))
                    .mapToDouble(AttributeModifier::getAmount)
                    .sum();

            if (result.has("attackSpeed")) {
                result.addProperty("attackSpeed", ( attackSpeed + 2.4 + speedOffset ) * speedMultiplier);
            }

            if (result.has("capabilities")) {
                JsonObject capabilitiesJson = result.getAsJsonObject("capabilities");
                Set<ToolType> toolTypes = itemStack.getToolTypes();

                if (capabilitiesJson.size() == toolTypes.size()) {
                    toolTypes.forEach(toolType -> {
                        BlockState blockState = harvestMap.get(toolType);

                        JsonArray value = new JsonArray();
                        value.add(itemStack.getHarvestLevel(toolType, null, blockState));
                        value.add(blockState != null ? item.getDestroySpeed(itemStack, blockState) / ( attackSpeed + 4 ): 0);

                        capabilitiesJson.add(toolType.getName(), value);
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

                    for (Map.Entry<String, JsonElement> entry : capabilitiesJson.entrySet()) {
                        capabilitiesJson.add(entry.getKey(), value);
                    }
                }
            }
        }

        return result;
    }

    public JsonObject getSchemaJson() {
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

        if (MathHelper.ceil(material.count * countMultiplier) > 1) {
            outcomeMaterial.addProperty("count", MathHelper.ceil(material.count * countMultiplier));
        }


        if (material.capability != null && material.capabilityLevel + capabilityOffset > 0) {
            JsonObject requiredCapabilities = new JsonObject();
            outcome.add("requiredCapabilities", requiredCapabilities);
            requiredCapabilities.addProperty(material.capability.toString(), material.capabilityLevel + capabilityOffset);
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

        /** basically a hex color code (but with a prefix for java to parse it as an int, example: 0x00ff00 would be green **/
        int tint = 0x000000;

        int materialTint;

        /** this is relative the reference item **/
        int integrity;

        int magicCapacity;

        String type;
        String itemId;
        int count;
        Capability capability;
        int capabilityLevel;
        Pair<String, Integer>[] improvements = new Pair[0];

        public Material(String key, int tint, int materialTint, int integrity, int magicCapacity) {
            this.key = key;
            this.tint = tint;
            this.materialTint = materialTint;
            this.integrity = integrity;
            this.magicCapacity = magicCapacity;
        }

        public Material(String key, int tint, int materialTint, int integrity, int magicCapacity, String type,
                String itemId, int count, Capability capability, int capabilityLevel) {
            this(key, tint, materialTint, integrity, magicCapacity);

            this.type = type;
            this.itemId = itemId;
            this.count = count;
            this.capability = capability;
            this.capabilityLevel = capabilityLevel;
        }

        public Material(String key, int tint, int materialTint, int integrity, int magicCapacity, String type,
                String itemId, int count, Capability capability, int capabilityLevel, Pair<String, Integer> ... improvements) {
            this(key, tint, materialTint, integrity, magicCapacity, type, itemId, count, capability, capabilityLevel);

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

