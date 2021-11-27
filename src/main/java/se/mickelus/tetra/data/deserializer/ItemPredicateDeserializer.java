package se.mickelus.tetra.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.advancements.criterion.ItemPredicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;

public class ItemPredicateDeserializer implements JsonDeserializer<ItemPredicate> {
    private static final Logger logger = LogManager.getLogger();
    @Override
    public ItemPredicate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return deserialize(json);
    }

    public static ItemPredicate deserialize(JsonElement json) {
        try {
            return ItemPredicate.deserialize(json);
        } catch (JsonParseException e) {
            logger.debug("Failed to parse item predicate from \"{}\": '{}'", json, e.getMessage());
            // todo: debug level log
            return null;
        }
    }
}
