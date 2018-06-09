package se.mickelus.tetra.module;

import com.google.gson.*;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.items.ItemModular;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ReplacementDefinition {
    public ItemPredicate predicate;
    public ItemStack itemStack;
    public Map<String, String[]> modules = new HashMap<>();
    public Map<String, Integer> improvements = new HashMap<>();

    public static class ReplacementDeserializer implements JsonDeserializer<ReplacementDefinition> {

        @Override
        public ReplacementDefinition deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            ReplacementDefinition replacement = new ReplacementDefinition();
            JsonObject jsonObject = element.getAsJsonObject();

            replacement.predicate = ItemPredicate.deserialize(JsonUtils.getJsonObject(jsonObject, "predicate"));

            ResourceLocation resourcelocation = new ResourceLocation(JsonUtils.getString(jsonObject, "item"));
            Item item = Item.REGISTRY.getObject(resourcelocation);
            if (item == null) {
                throw new JsonSyntaxException("Failed to parse replacement data from " + jsonObject.getAsString());
            }
            replacement.itemStack = new ItemStack(item);

            if (item instanceof ItemModular) {
                for (Map.Entry<String, JsonElement> moduleDefinition: JsonUtils.getJsonObject(jsonObject, "modules").entrySet()) {
                    String moduleKey = moduleDefinition.getValue().getAsJsonArray().get(0).getAsString();
                    String moduleVariant = moduleDefinition.getValue().getAsJsonArray().get(1).getAsString();
                    ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleKey);
                    if (module == null) {
                        throw new JsonSyntaxException("Failed to parse replacement data due to missing module: " + moduleKey);
                    }
                    module.addModule(replacement.itemStack, moduleVariant, null);
                }

                if (jsonObject.has("improvements")) {
                    for (Map.Entry<String, JsonElement> improvement: JsonUtils.getJsonObject(jsonObject, "improvements").entrySet()) {
                        String temp[] = improvement.getKey().split(":");
                        ItemModuleMajor.addImprovement(replacement.itemStack, temp[0], temp[1], improvement.getValue().getAsInt());
                    }
                }
            }

            return replacement;
        }
    }
}
