package se.mickelus.tetra.module.data;

import com.google.gson.*;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;

@ParametersAreNonnullByDefault
public class ImprovementData extends VariantData {

    /**
     * Several improvements with the same key may exist as long as they are of different levels. The level field also
     * matters when converting non-modular items into modular items, enchantments that have a matching improvement with
     * the same level will be added to the modular item.
     * No level label is displayed when the level is set to 0.
     */
    public int level = 0;

    /**
     * If set to true the item will render with the enchantment glint.
     */
    public boolean enchantment = false;

    /**
     * Improvements in the same group cannot be present for a slot at the same time. Adding an improvement with a
     * specified group will remove any other improvement on that slot which has a matching group.
     */
    public String group = null;

    public int getLevel() {
        return level;
    }

    public static class Deserializer implements JsonDeserializer<ImprovementData> {
        @Override
        public ImprovementData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            if (jsonObject.has("materials")) {
                return context.deserialize(json, MaterialImprovementData.class);
            } else {
                return context.deserialize(json, UniqueImprovementData.class);
            }
        }
    }
}
