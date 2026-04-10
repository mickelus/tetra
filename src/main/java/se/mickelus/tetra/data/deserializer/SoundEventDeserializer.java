package se.mickelus.tetra.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import se.mickelus.tetra.compat.forge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;

public class SoundEventDeserializer implements JsonDeserializer<SoundEvent> {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public SoundEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(json.getAsString()));
        } catch (JsonParseException e) {
            logger.debug("Failed to parse sound event: {}", json, e);
            return null;
        }
    }
}
