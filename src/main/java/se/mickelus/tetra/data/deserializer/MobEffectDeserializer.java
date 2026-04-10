package se.mickelus.tetra.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import se.mickelus.tetra.compat.forge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class MobEffectDeserializer implements JsonDeserializer<MobEffect> {
    @Override
    public MobEffect deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String string = json.getAsString();
        if (string != null) {
            ResourceLocation resourceLocation = ResourceLocation.parse(string);
            if (ForgeRegistries.MOB_EFFECTS.containsKey(resourceLocation)) {
                return ForgeRegistries.MOB_EFFECTS.getValue(resourceLocation);
            }
        }

        return null;
    }
}
