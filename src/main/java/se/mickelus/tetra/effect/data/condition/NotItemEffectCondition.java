package se.mickelus.tetra.effect.data.condition;

import com.google.gson.JsonElement;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.data.ItemEffectContext;

public class NotItemEffectCondition extends ItemEffectCondition {
    private ItemEffectCondition condition;

    @Override
    public boolean test(ItemEffectContext context) {
        return !condition.test(context);
    }

    public static ItemEffectCondition deserialize(JsonElement jsonElement) {
        return DataManager.gson.fromJson(jsonElement, NotItemEffectCondition.class);
    }
}
