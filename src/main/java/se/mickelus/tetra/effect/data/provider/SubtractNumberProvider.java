package se.mickelus.tetra.effect.data.provider;

import com.google.gson.JsonElement;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.data.ItemEffectContext;

public class SubtractNumberProvider implements NumberProvider {
    private final NumberProvider positive;
    private final NumberProvider negative;

    public SubtractNumberProvider(NumberProvider positive, NumberProvider negative) {
        this.positive = positive;
        this.negative = negative;
    }

    @Override
    public float getValue(ItemEffectContext context) {
        return positive.getValue(context) - negative.getValue(context);
    }

    public static NumberProvider deserialize(JsonElement jsonElement) {
        return DataManager.gson.fromJson(jsonElement, SubtractNumberProvider.class);
    }
}
