package se.mickelus.tetra.items;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.advancements.critereon.ItemPredicates;
import org.apache.commons.lang3.EnumUtils;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.module.ItemModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class ItemModularPredicate extends ItemPredicate {

    private String[][] modules = new String[0][0];
    private Map<String, String> variants = new HashMap<>();

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
        if (jsonObject.has("variants")) {
            jsonObject.getAsJsonObject("variants").entrySet().forEach(entry -> variants.put(entry.getKey(), entry.getValue().getAsString()));
        }
    }

    public boolean test(ItemStack itemStack, String slot) {
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) itemStack.getItem();

            // if it's a slot specific check and there are single module requirement, assume the requirement is to be matched against the checked slot
            if (slot != null) {
                ItemModule module = item.getModuleFromSlot(itemStack, slot);
                if (module != null) {
                    for (int i = 0; i < modules.length; i++) {
                        if (modules[i].length == 1 && modules[i][0].equals(module.getKey())) {
                            return true;
                        }
                    }
                }
            } else {
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

            for (Map.Entry<String, String> variant : variants.entrySet()) {
                String currentSlot = variant.getValue();
                if (slot != null && "#slot".equals(currentSlot)) {
                    currentSlot = slot;
                }

                ItemModule module = item.getModuleFromSlot(itemStack, currentSlot);
                if (module != null && variant.getKey().equals(module.getData(itemStack).key)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean test(ItemStack itemStack) {
       return test(itemStack, null);
    }
}
