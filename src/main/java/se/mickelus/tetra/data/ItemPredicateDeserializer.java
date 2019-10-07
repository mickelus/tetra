package se.mickelus.tetra.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.advancements.criterion.ItemPredicate;

import java.lang.reflect.Type;

public class ItemPredicateDeserializer implements JsonDeserializer<ItemPredicate> {
    @Override
    public ItemPredicate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return ItemPredicate.deserialize(json);
        } catch (JsonParseException e) {
            // todo: debug level log
            return null;
        }
    }
}
