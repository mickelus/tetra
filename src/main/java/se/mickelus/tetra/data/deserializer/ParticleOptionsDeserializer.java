package se.mickelus.tetra.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;

public class ParticleOptionsDeserializer implements JsonDeserializer<ParticleOptions> {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public ParticleOptions deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return ParticleTypes.CODEC.decode(JsonOps.INSTANCE, json)
                    .result()
                    .map(Pair::getFirst)
                    .orElseThrow(() -> new JsonParseException("Missing particle data in json element"));
        } catch (JsonParseException e) {
            logger.debug("Failed to parse particle options: {}", json, e);
            return null;
        }
    }
}
