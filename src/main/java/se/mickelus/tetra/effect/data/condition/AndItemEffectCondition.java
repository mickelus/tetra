package se.mickelus.tetra.effect.data.condition;

import com.google.gson.JsonElement;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.data.ItemEffectContext;

import java.util.Arrays;

public class AndItemEffectCondition extends ItemEffectCondition {
    private ItemEffectCondition[] conditions;

    @Override
    public boolean test(ItemEffectContext context) {
        return Arrays.stream(conditions).allMatch(condition -> condition.test(context));
    }

    public static ItemEffectCondition deserialize(JsonElement jsonElement) {
        return DataManager.gson.fromJson(jsonElement, AndItemEffectCondition.class);
    }
}
