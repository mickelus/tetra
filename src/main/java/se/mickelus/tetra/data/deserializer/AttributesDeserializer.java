package se.mickelus.tetra.data.deserializer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class AttributesDeserializer implements JsonDeserializer<Multimap<Attribute, AttributeModifier>> {
    public static final TypeToken<Multimap<Attribute, AttributeModifier>> typeToken = new TypeToken<Multimap<Attribute, AttributeModifier>>() {
    };

    private static AttributeModifier.Operation getOperation(String key) {
        if (key.startsWith("**")) {
            return AttributeModifier.Operation.MULTIPLY_TOTAL;
        } else if (key.startsWith("*")) {
            return AttributeModifier.Operation.MULTIPLY_BASE;
        }

        return AttributeModifier.Operation.ADDITION;
    }

    private static Attribute getAttribute(String key) {
        ResourceLocation rl = new ResourceLocation(key.replace("*", ""));

        return ForgeRegistries.ATTRIBUTES.getValue(rl);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        ArrayListMultimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();

        jsonObject.entrySet().forEach(entry -> {
            Attribute attribute = getAttribute(entry.getKey());
            if (attribute != null) {
                result.put(attribute, new AttributeModifier("module_data", entry.getValue().getAsDouble(), getOperation(entry.getKey())));
            }
        });

        return result;
    }
}
