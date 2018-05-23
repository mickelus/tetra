package se.mickelus.tetra.module.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.advancements.critereon.ItemPredicate;

import java.lang.reflect.Type;

public class PredicateDeserializer implements JsonDeserializer<ItemPredicate> {
    @Override
    public ItemPredicate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return ItemPredicate.deserialize(json);
    }
}
