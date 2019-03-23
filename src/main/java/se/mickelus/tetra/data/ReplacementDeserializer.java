package se.mickelus.tetra.data;

import java.util.Map;
import java.lang.reflect.Type;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.ReplacementDefinition;

public class ReplacementDeserializer implements JsonDeserializer<ReplacementDefinition> {

    @Override
    public ReplacementDefinition deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws
            JsonParseException {
        ReplacementDefinition replacement = new ReplacementDefinition();
        JsonObject jsonObject = element.getAsJsonObject();

        try {
            replacement.predicate = ItemPredicate.deserialize(JsonUtils.getJsonObject(jsonObject, "predicate"));
        } catch (JsonSyntaxException e) {
            // todo: debug log here
//                System.out.println(String.format("Skipping modular replacement definition due to faulty predicate: %s", JsonUtils.getJsonObject(jsonObject, "predicate").toString()));
            return replacement;
        }

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
