package se.mickelus.tetra.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.advancements.critereon.EntityPredicate;

import java.lang.reflect.Type;

public class EntityPredicateDeserializer implements JsonDeserializer<EntityPredicate> {
    @Override
    public EntityPredicate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return EntityPredicate.fromJson(json);
    }
}
