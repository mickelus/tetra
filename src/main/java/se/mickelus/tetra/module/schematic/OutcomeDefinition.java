package se.mickelus.tetra.module.schematic;

import com.google.gson.*;
import se.mickelus.tetra.module.data.ToolData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
public class OutcomeDefinition {

    /**
     * Required material for the outcome. Basically an item predicate, see material documentation for more information.
     * Optional, only required if the schematic which contains this outcome has 1 or more material slots.
     */
    public OutcomeMaterial material = new OutcomeMaterial();

    /**
     * If set to true, this outcome will not show up in the holosphere.
     */
    public boolean hidden = false;

    /**
     * The slot in which to look for the material.
     * Optional, defaults to slot 0;
     */
    public int materialSlot = 0;

    /**
     * The experience cost for the craft.
     */
    public int experienceCost = 0;

    /**
     * Defines which tools (and which level of each tool) are required for this outcome.
     * Optional, if no tools are required this field can be omitted.
     * <p>
     * Json format:
     * {
     * "toolA": level,
     * "toolB": level
     * }
     */
    public ToolData requiredTools = new ToolData();

    /**
     * A key referring to a module.
     * Optional, but the schematic should then apply improvements or the outcome with be nothing.
     */
    public String moduleKey;

    /**
     * A module variant, has to be a variant for module specified by the moduleKey field.
     * Optional, but has to be set if the moduleKey field is set.
     */
    public String moduleVariant;

    /**
     * An object describing which improvements to apply to which slot, where the key is the improvement and the value
     * is the improvement level.
     * Optional, this can be used both with and without the moduleKey being set.
     * <p>
     * Json format:
     * {
     * "improvementA": level,
     * "improvementB": level
     * }
     */
    public Map<String, Integer> improvements = new HashMap<>();

    public static class Deserializer implements JsonDeserializer<OutcomeDefinition> {
        @Override
        public OutcomeDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            if (jsonObject.has("materials")) {
                return context.deserialize(json, MaterialOutcomeDefinition.class);
            } else {
                return context.deserialize(json, UniqueOutcomeDefinition.class);
            }
        }
    }
}
