package se.mickelus.tetra;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.data.predicate.TetraItemPredicate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.stream.StreamSupport;

// Cross-version compat: TetraItemPredicate (and siblings MaterialItemPredicate, EffectItemPredicate,
// SimpleItemPredicate) match the upstream 1.20 predicate shape. Do not fold onto vanilla
// ItemSubPredicate — rewriting forks the codebase from upstream Tetra.
@ParametersAreNonnullByDefault
public class LooseItemPredicate implements TetraItemPredicate {

    private final String[] keys;

    public LooseItemPredicate(JsonObject jsonObject) {
        keys = StreamSupport.stream(jsonObject.get("keys").getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsString)
                .toArray(String[]::new);

    }

    public boolean matches(ItemStack itemStack) {
        String target = Optional.of(itemStack.getItem())
                .map(BuiltInRegistries.ITEM::getKey)
                .map(ResourceLocation::getPath)
                .orElse(null);
        for (String key : keys) {
            if (key.equals(target)) {
                return true;
            }
        }

        return false;
    }
}
