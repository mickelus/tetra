package se.mickelus.tetra.data.deserializer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import se.mickelus.tetra.util.RegistryHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
import java.util.Map;

// Cross-version compat: this Gson deserializer mirrors upstream 1.20. Do not migrate to Codec /
// MapCodec — rewriting forks the codebase from upstream Tetra.
@ParametersAreNonnullByDefault
public class AttributesDeserializer implements JsonDeserializer<Multimap<Attribute, AttributeModifier>> {
    public static final TypeToken<Multimap<Attribute, AttributeModifier>> typeToken = new TypeToken<Multimap<Attribute, AttributeModifier>>() {
    };
    // legacyAttributeIds: Forge-namespaced 1.20 attribute IDs that we translate into 1.21 vanilla IDs
    // at JSON parse time. Keep the map in sync with upstream 1.20 — JSON authored against 1.20 still
    // uses these forge: keys, and rewriting to vanilla namespaces would diverge from upstream.
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

        return RegistryHelper.get(BuiltInRegistries.ATTRIBUTE, rl);
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
