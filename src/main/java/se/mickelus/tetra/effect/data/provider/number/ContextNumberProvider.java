package se.mickelus.tetra.effect.data.provider.number;

import com.google.gson.JsonElement;
import se.mickelus.tetra.effect.data.ItemEffectContext;

public class ContextNumberProvider implements NumberProvider {
    String key;

    public ContextNumberProvider(String key) {
        this.key = key;
    }

    @Override
    public float getValue(ItemEffectContext context) {
        return context.getData().get(key);
    }

    public static NumberProvider deserialize(JsonElement jsonElement) {
        return new ContextNumberProvider(jsonElement.getAsJsonObject().get("key").getAsString());
    }
}
