package se.mickelus.tetra;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.data.MaterialData;

import java.util.Comparator;
import java.util.stream.StreamSupport;


public class LooseItemPredicate extends ItemPredicate {

    private String[] keys;

    public LooseItemPredicate(JsonObject jsonObject) {
        keys = StreamSupport.stream(jsonObject.get("keys").getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsString)
                .toArray(String[]::new);

    }

    @Override
    public boolean matches(ItemStack itemStack) {
        String target = itemStack.getItem().getRegistryName().getPath();
        for (String key: keys) {
            if (key.equals(target)) {
                return true;
            }
        }

        return false;
    }
}
