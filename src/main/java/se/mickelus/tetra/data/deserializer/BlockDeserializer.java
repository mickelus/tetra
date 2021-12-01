package se.mickelus.tetra.data.deserializer;

import com.google.gson.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Type;

public class BlockDeserializer implements JsonDeserializer<Block> {
    @Override
    public Block deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String string = json.getAsString();
        if (string != null) {
            ResourceLocation resourceLocation = new ResourceLocation(string);
            if (ForgeRegistries.BLOCKS.containsKey(resourceLocation)) {
                return ForgeRegistries.BLOCKS.getValue(resourceLocation);
            }
        }

        return null;
    }
}
