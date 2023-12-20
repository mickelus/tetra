package se.mickelus.tetra.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.world.item.Rarity;

import java.lang.reflect.Type;
import java.util.Optional;

public class RarityDeserializer implements JsonDeserializer<Rarity> {
    @Override
    public Rarity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return Optional.ofNullable(json.getAsString()).map(String::toUpperCase).map(Rarity::valueOf).orElse(null);
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Tried to parse faulty Rarity: " + json, e);
        }
    }
}
