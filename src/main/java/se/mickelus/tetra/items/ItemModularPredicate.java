package se.mickelus.tetra.items;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.advancements.critereon.ItemPredicates;
import se.mickelus.tetra.module.ItemModule;

import java.util.Collection;


public class ItemModularPredicate extends ItemPredicate {

    private String[][] modules;

    public ItemModularPredicate(String[][] modules) {
        this.modules = modules;
    }

    public ItemModularPredicate(JsonObject jsonObject) {
        if (jsonObject.has("modules")) {
            JsonArray outerModules = jsonObject.getAsJsonArray("modules");
            modules = new String[outerModules.size()][];
            for (int i = 0; i < outerModules.size(); i++) {
                JsonArray innerModules = outerModules.get(i).getAsJsonArray();
                modules[i] = new String[innerModules.size()];

                for (int j = 0; j < innerModules.size(); j++) {
                    modules[i][j] = innerModules.get(j).getAsString();
                }
            }
        }
    }

    @Override
    public boolean test(ItemStack itemStack) {
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) itemStack.getItem();
            Collection<ItemModule> itemModules = item.getAllModules(itemStack);
            for (int i = 0; i < modules.length; i++) {
                for (int j = 0; j < modules[i].length; j++) {
                    int matchCount = 0;

                    for (ItemModule module: itemModules) {
                        if (module.getKey().equals(modules[i][j])) {
                            matchCount++;
                            break;
                        }
                    }

                    if (matchCount == modules[i].length) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
