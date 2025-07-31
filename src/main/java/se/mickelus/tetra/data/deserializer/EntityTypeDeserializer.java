package se.mickelus.tetra.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class EntityTypeDeserializer implements JsonDeserializer<EntityType<? extends Entity>> {
    @Override
    public EntityType<? extends Entity> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return EntityType.byString(json.getAsString())
                .orElseThrow(() -> new JsonParseException("Unknown entity type: " + json.getAsString()));
    }
}
