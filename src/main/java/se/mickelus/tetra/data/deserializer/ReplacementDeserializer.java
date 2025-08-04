package se.mickelus.tetra.data.deserializer;

import com.google.gson.*;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.dynamic.DynamicModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.ReplacementDefinition;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
import java.util.Map;

@ParametersAreNonnullByDefault
public class ReplacementDeserializer implements JsonDeserializer<ReplacementDefinition> {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public ReplacementDefinition deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws
            JsonParseException {
        ReplacementDefinition replacement = new ReplacementDefinition();
        JsonObject jsonObject = element.getAsJsonObject();

        try {
            replacement.predicate = ItemPredicate.fromJson(GsonHelper.getAsJsonObject(jsonObject, "predicate"));
        } catch (JsonSyntaxException e) {
            throw new JsonSyntaxException("Failed to parse replacement data due to faulty predicate", e);
        }

        ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "item"));
        Item item = ForgeRegistries.ITEMS.getValue(resourcelocation);
        if (item == null) {
            throw new JsonSyntaxException("Failed to parse replacement data, missing (or faulty) item in " + jsonObject.getAsString());
        }
        replacement.itemStack = new ItemStack(item);

        if (item instanceof IModularItem) {
            for (Map.Entry<String, JsonElement> moduleDefinition : GsonHelper.getAsJsonObject(jsonObject, "modules").entrySet()) {
                String moduleKey = moduleDefinition.getValue().getAsJsonArray().get(0).getAsString();
                String moduleVariant = moduleDefinition.getValue().getAsJsonArray().get(1).getAsString();
                ItemModule module = ItemUpgradeRegistry.instance.getModule(moduleKey);
                if (module == null) {
                    throw new JsonSyntaxException("Failed to parse replacement data due to missing module: " + moduleKey);
                }
                module.addModule(replacement.itemStack, moduleVariant, null);
            }

            if (jsonObject.has("improvements")) {
                for (Map.Entry<String, JsonElement> improvement : GsonHelper.getAsJsonObject(jsonObject, "improvements").entrySet()) {
                    String[] temp = improvement.getKey().split(":");
                    ItemModuleMajor.addImprovement(replacement.itemStack, temp[0], temp[1], improvement.getValue().getAsInt());
                }
            }

            if (jsonObject.has("archetype")) {
                DynamicModularItem.setArchetypeKey(replacement.itemStack, jsonObject.get("archetype").getAsString());
            }
        }

        return replacement;
    }
}
