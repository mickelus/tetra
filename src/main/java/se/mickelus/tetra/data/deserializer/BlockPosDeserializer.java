package se.mickelus.tetra.data.deserializer;

import com.google.gson.*;
import net.minecraft.core.BlockPos;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
@ParametersAreNonnullByDefault
public class BlockPosDeserializer implements JsonDeserializer<BlockPos> {
    @Override
    public BlockPos deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        return new BlockPos(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
    }
}
