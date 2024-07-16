package se.mickelus.tetra.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class BlockStateDeserializer implements JsonDeserializer<BlockState> {
    private static final Logger logger = LogManager.getLogger();

    @Override
    public BlockState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return BlockState.CODEC.decode(JsonOps.INSTANCE, json)
                    .result()
                    .map(Pair::getFirst)
                    .orElseThrow(() -> new JsonParseException("Missing block state data in json element"));
        } catch (JsonParseException e) {
            logger.debug("Failed to parse block state: {}", json, e);
            return null;
        }
    }
}
