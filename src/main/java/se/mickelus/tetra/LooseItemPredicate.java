package se.mickelus.tetra;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.tetra.data.predicate.TetraItemPredicate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.stream.StreamSupport;

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
                .map(ForgeRegistries.ITEMS::getKey)
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
