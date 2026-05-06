package se.mickelus.tetra.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.LooseItemPredicate;
import se.mickelus.tetra.data.predicate.SimpleItemPredicate;
import se.mickelus.tetra.data.predicate.TetraItemPredicate;
import se.mickelus.tetra.items.modular.EffectItemPredicate;
import se.mickelus.tetra.items.modular.ItemPredicateModular;
import se.mickelus.tetra.items.modular.MaterialItemPredicate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@ParametersAreNonnullByDefault
public class ItemPredicateDeserializer implements JsonDeserializer<TetraItemPredicate> {
    private static final Logger logger = LogManager.getLogger();

    public static TetraItemPredicate deserialize(JsonElement json) {
        try {
            JsonObject jsonObject = GsonHelper.convertToJsonObject(json, "item predicate");

            if (jsonObject.has("type")) {
                String type = GsonHelper.getAsString(jsonObject, "type");
                return switch (type) {
                    case "tetra:modular_item" -> new ItemPredicateModular(jsonObject);
                    case "tetra:item_effect" -> new EffectItemPredicate(jsonObject);
                    case "tetra:material" -> new MaterialItemPredicate(jsonObject);
                    case "tetra:loose" -> new LooseItemPredicate(jsonObject);
                    default -> deserializeSimple(jsonObject);
                };
            }

            return deserializeSimple(jsonObject);
        } catch (JsonParseException e) {
            logger.debug("Failed to parse item predicate from \"{}\": '{}'", json, e.getMessage());
            return null;
        }
    }

    @Override
    public TetraItemPredicate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return deserialize(json);
    }

    private static TetraItemPredicate deserializeSimple(JsonObject jsonObject) {
        Stream<ResourceLocation> items = Stream.empty();
        if (jsonObject.has("items")) {
            items = StreamSupport.stream(GsonHelper.getAsJsonArray(jsonObject, "items").spliterator(), false)
                    .map(JsonElement::getAsString)
                    .map(ResourceLocation::parse);
        } else if (jsonObject.has("item")) {
            items = Stream.of(ResourceLocation.parse(GsonHelper.getAsString(jsonObject, "item")));
        }

        ResourceLocation tagId = jsonObject.has("tag")
                ? ResourceLocation.parse(GsonHelper.getAsString(jsonObject, "tag"))
                : null;

        return new SimpleItemPredicate(items.toList(), tagId);
    }
}
