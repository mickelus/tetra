package se.mickelus.tetra.items.modular;

import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.data.predicate.TetraItemPredicate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Comparator;

@ParametersAreNonnullByDefault
public class MaterialItemPredicate implements TetraItemPredicate {

    private final String category;

    public MaterialItemPredicate(JsonObject jsonObject) {
        category = jsonObject.get("category").getAsString();
    }

    public boolean matches(ItemStack itemStack) {
        return DataManager.instance.materialData.getData().values().stream()
                .sorted(Comparator.comparing(data -> data.material.isTagged()))
                .filter(data -> data.material.isValid())
                .filter(data -> data.material.getPredicate().matches(itemStack))
                .anyMatch(data -> category == null || category.equals(data.category));
    }
}
