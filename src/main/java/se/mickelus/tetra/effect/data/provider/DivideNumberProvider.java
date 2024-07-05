package se.mickelus.tetra.effect.data.provider;

import com.google.gson.JsonElement;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.data.ItemEffectContext;

public class DivideNumberProvider implements NumberProvider {
    private final NumberProvider numerator;
    private final NumberProvider denominator;

    public DivideNumberProvider(NumberProvider numerator, NumberProvider denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    @Override
    public float getValue(ItemEffectContext context) {
        return numerator.getValue(context) / denominator.getValue(context);
    }

    public static NumberProvider deserialize(JsonElement jsonElement) {
        return DataManager.gson.fromJson(jsonElement, DivideNumberProvider.class);
    }
}
