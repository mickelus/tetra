package se.mickelus.tetra.module.schematic.requirement;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import se.mickelus.mutil.util.JsonOptional;
import se.mickelus.tetra.module.schematic.CraftingContext;

import java.lang.reflect.Type;

public class ModuleRequirement implements CraftingRequirement {
    String moduleKey;
    String moduleVariant;

    public ModuleRequirement(String moduleKey, String moduleVariant) {
        this.moduleKey = moduleKey;
        this.moduleVariant = moduleVariant;
    }

    @Override
    public boolean test(CraftingContext context) {
        if (context.targetModule != null) {
            if (moduleKey != null && !moduleKey.equals(context.targetModule.getKey())) {
                return false;
            }
            if (moduleVariant != null && !moduleVariant.equals(context.targetModule.getVariantData(context.targetStack).key)) {
                return false;
            }
        }
        return true;
    }

    public static class Deserializer implements JsonDeserializer<CraftingRequirement> {
        @Override
        public CraftingRequirement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new ModuleRequirement(
                    JsonOptional.field(json.getAsJsonObject(), "module")
                            .map(JsonElement::getAsString)
                            .orElse(null),
                    JsonOptional.field(json.getAsJsonObject(), "variant")
                            .map(JsonElement::getAsString)
                            .orElse(null));
        }
    }
}
