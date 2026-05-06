package se.mickelus.tetra.items.modular;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.data.predicate.TetraItemPredicate;
import se.mickelus.tetra.module.schematic.requirement.IntegerPredicate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class EffectItemPredicate implements TetraItemPredicate {
    ItemEffect effect;
    IntegerPredicate level;

    public EffectItemPredicate() {

    }

    public EffectItemPredicate(JsonObject jsonObject) {
        if (jsonObject.has("effect")) {
            effect = ItemEffect.get(jsonObject.get("effect").getAsString());
        } else {
            throw new JsonParseException("Missing required field 'effect' when parsing 'tetra:effect_predicate'");
        }
        if (jsonObject.has("level")) {
            level = IntegerPredicate.Deserializer.deserialize(jsonObject);
        }
    }

    public boolean matches(ItemStack itemStack) {
        if (effect != null && !itemStack.isEmpty() && itemStack.getItem() instanceof IModularItem item) {
            if (level != null) {
                return level.test(item.getEffectLevel(itemStack, effect));
            }
            return item.getEffects(itemStack).contains(effect);
        }
        return false;
    }
}
