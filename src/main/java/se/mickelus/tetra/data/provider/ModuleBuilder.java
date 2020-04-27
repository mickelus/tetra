package se.mickelus.tetra.data.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Map;

public class ModuleBuilder {

    public String module;
    public String prefix;

    public JsonObject referenceVariant;

    private ArrayList<Pair<Material, Item>> variants = new ArrayList<>();

    public ModuleBuilder(String module, String prefix, JsonObject referenceVariant) {
        this.module = module;
        this.prefix = prefix;

        this.referenceVariant = referenceVariant;
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

        public Material(String key, int tint, int integrity) {
            this.key = key;
            this.tint = tint;
            this.integrity = integrity;
        }

        String key;

        /** basically a hex color code (but with a prefix for java to parse it as an int, example: 0x00ff00 would be green **/
        int tint = 0x000000;

        /** this is relative the reference item **/
        int integrity;
    }
}

