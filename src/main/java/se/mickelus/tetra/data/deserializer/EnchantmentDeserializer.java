package se.mickelus.tetra.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Type;

public class EnchantmentDeserializer implements JsonDeserializer<Enchantment> {
    @Override
    public Enchantment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String string = json.getAsString();
        if (string != null) {
            ResourceLocation resourceLocation = new ResourceLocation(string);
            if (ForgeRegistries.ENCHANTMENTS.containsKey(resourceLocation)) {
                return ForgeRegistries.ENCHANTMENTS.getValue(resourceLocation);
            }
        }

        return null;
    }
}