package se.mickelus.tetra.effect.data.provider;

import com.google.gson.JsonElement;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.util.Arrays;

public class SumNumberProvider implements NumberProvider {
    private final NumberProvider[] providers;

    public SumNumberProvider(NumberProvider... providers) {
        this.providers = providers;
    }

    @Override
    public float getValue(ItemEffectContext context) {
        return (float) Arrays.stream(providers).mapToDouble(provider -> provider.getValue(context)).sum();
    }

    public static NumberProvider deserialize(JsonElement jsonElement) {
        return DataManager.gson.fromJson(jsonElement, SumNumberProvider.class);
    }
}
