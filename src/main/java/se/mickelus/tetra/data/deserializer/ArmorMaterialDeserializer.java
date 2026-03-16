package se.mickelus.tetra.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;

import java.lang.reflect.Type;

public class ArmorMaterialDeserializer implements JsonDeserializer<ArmorMaterial> {
    @Override
    public ArmorMaterial deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return ArmorMaterials.CODEC.byName(json.getAsString());
        } catch (Exception e) {
            throw new JsonParseException("Tried to parse faulty ArmorMaterial: " + json, e);
        }
    }
}
