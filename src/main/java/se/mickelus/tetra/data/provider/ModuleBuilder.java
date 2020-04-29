package se.mickelus.tetra.data.provider;

import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import se.mickelus.tetra.module.data.CapabilityData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ModuleBuilder {

    public String module;
    public String prefix;

    public JsonObject referenceVariant;

    private int durabilityOffset = 0;
    private float durabilityMultiplier = 1;

    private float speedOffset = 0;
    private float speedMultiplier = 1;

    private int integrityOffset = 0;

    private ArrayList<Pair<Material, Item>> variants = new ArrayList<>();

    private Map<ToolType, BlockState> harvestMap;

    public ModuleBuilder(String module, String prefix, JsonObject referenceVariant) {
        this.module = module;
        this.prefix = prefix;

        this.referenceVariant = referenceVariant;

        harvestMap = new HashMap<>();
        harvestMap.put(ToolType.AXE, Blocks.OAK_LOG.getDefaultState());
        harvestMap.put(ToolType.PICKAXE, Blocks.STONE.getDefaultState());
        harvestMap.put(ToolType.SHOVEL, Blocks.DIRT.getDefaultState());
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
        variants.add(Pair.of(material, null));
        return this;
    }

    public ModuleBuilder addVariant(Material material, String itemId) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));

        if (item == null) {
            throw new NullPointerException("Missing item '" + itemId + "'");
        }
        variants.add(Pair.of(material, item));

        return this;
    }

    public JsonObject getJson() {
        JsonObject result = new JsonObject();
        JsonArray variantsJson = new JsonArray();

        variants.stream()
                .map(this::getVariantJson)
                .forEach(variantsJson::add);

        result.add("variants", variantsJson);

        return result;
    }

    private JsonObject getVariantJson(Pair<Material, Item> dataPair) {
        Material material = dataPair.getLeft();
        Item item = dataPair.getRight();

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

        public Material(String key, int tint, int materialTint, int integrity, int magicCapacity) {
            this.key = key;
            this.tint = tint;
            this.materialTint = materialTint;
            this.integrity = integrity;
            this.magicCapacity = magicCapacity;
        }
    }
}

