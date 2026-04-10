package se.mickelus.tetra.data.deserializer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import se.mickelus.tetra.compat.forge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
import java.util.Map;

@ParametersAreNonnullByDefault
public class AttributesDeserializer implements JsonDeserializer<Multimap<Attribute, AttributeModifier>> {
    public static final TypeToken<Multimap<Attribute, AttributeModifier>> typeToken = new TypeToken<Multimap<Attribute, AttributeModifier>>() {
    };
    private static final Map<String, ResourceLocation> legacyAttributeIds = Map.of(
            "forge:reach_distance", ResourceLocation.withDefaultNamespace("player.block_interaction_range"),
            "forge:block_reach", ResourceLocation.withDefaultNamespace("player.block_interaction_range"),
            "forge:attack_range", ResourceLocation.withDefaultNamespace("player.entity_interaction_range"),
            "forge:entity_reach", ResourceLocation.withDefaultNamespace("player.entity_interaction_range"));

    private static AttributeModifier.Operation getOperation(String key) {
        if (key.startsWith("**")) {
            return AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
        } else if (key.startsWith("*")) {
            return AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
        }

        return AttributeModifier.Operation.ADD_VALUE;
    }

    private static Attribute getAttribute(String key) {
        String resolvedKey = key.replace("*", "");
        ResourceLocation rl = legacyAttributeIds.getOrDefault(resolvedKey, ResourceLocation.parse(resolvedKey));

        return ForgeRegistries.ATTRIBUTES.getValue(rl);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        ArrayListMultimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();

        jsonObject.entrySet().forEach(entry -> {
            Attribute attribute = getAttribute(entry.getKey());
            if (attribute != null) {
                result.put(attribute, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("tetra", "module_data"), entry.getValue().getAsDouble(), getOperation(entry.getKey())));
            }
        });

        return result;
    }
}
