package se.mickelus.tetra.items.modular;

import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.data.MaterialData;

import java.util.Comparator;


public class MaterialItemPredicate extends ItemPredicate {

    private String category;

    public MaterialItemPredicate(JsonObject jsonObject) {
        category = jsonObject.get("category").getAsString();
    }

    @Override
    public boolean matches(ItemStack itemStack) {
        MaterialData materialData = DataManager.materialData.getData().values().stream()
                .sorted(Comparator.comparing(data -> data.material.isTagged()))
                .filter(data -> data.material.isValid())
                .filter(data -> data.material.getPredicate().matches(itemStack))
                .findFirst()
                .orElse(null);
        if (materialData != null) {
            if (category != null && !category.equals(materialData.category)) {
                return false;
            }

            return true;
        }

        return false;
    }
}
