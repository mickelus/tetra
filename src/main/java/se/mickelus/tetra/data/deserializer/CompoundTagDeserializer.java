package se.mickelus.tetra.data.deserializer;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.GsonHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class CompoundTagDeserializer implements JsonDeserializer<CompoundTag> {
    @Override
    public CompoundTag deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            if (json.isJsonObject()) {
                return TagParser.parseTag(json.toString());
            }
            throw new JsonSyntaxException("Expected value to be a string, was " + GsonHelper.getType(json));
        } catch (CommandSyntaxException e) {
            throw new JsonParseException("Failed to parse NBT data: " + e.getMessage(), e);
        }
    }
}
