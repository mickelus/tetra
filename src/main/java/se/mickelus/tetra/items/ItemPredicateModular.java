package se.mickelus.tetra.items;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.util.CastOptional;

import java.util.*;


public class ItemPredicateModular extends ItemPredicate {

    private String[][] modules = new String[0][0];
    private Map<String, String> variants = new HashMap<>();
    private Map<String, Integer> improvements = new HashMap<>();

    public ItemPredicateModular(String[][] modules) {
        this.modules = modules;
    }

    public ItemPredicateModular(JsonObject jsonObject) {
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

        if (jsonObject.has("improvements")) {
            jsonObject.getAsJsonObject("improvements").entrySet().forEach(entry -> improvements.put(entry.getKey(), entry.getValue().getAsInt()));
        }
    }

    public boolean test(ItemStack itemStack, String slot) {
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemModular) {
            if (modules.length > 0 && !hasAnyModule(itemStack, slot)) {
                return false;
            }

            if (!variants.isEmpty() && !hasAnyVariant(itemStack, slot)) {
                return false;
            }

            if (!improvements.isEmpty() && !checkImprovements(itemStack, slot)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasAnyModule(ItemStack itemStack, String slot) {
        ItemModular item = (ItemModular) itemStack.getItem();

        // if it's a slot specific check and there are single module requirement, assume the requirement is to be matched against the checked slot
        if (slot != null) {
            ItemModule module = item.getModuleFromSlot(itemStack, slot);
            if (module != null) {
                for (String[] outer : modules) {
                    if (outer.length == 1 && outer[0].equals(module.getKey())) {
                        return true;
                    }
                }
            }
        } else {
            Collection<ItemModule> itemModules = item.getAllModules(itemStack);
            for (String[] outer : modules) {
                for (int j = 0; j < outer.length; j++) {
                    int matchCount = 0;

                    for (ItemModule module : itemModules) {
                        if (module.getKey().equals(outer[j])) {
                            matchCount++;
                            break;
                        }
                    }

                    if (matchCount == outer.length) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean hasAnyVariant(ItemStack itemStack, String slot) {
        ItemModular item = (ItemModular) itemStack.getItem();

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

        return false;
    }


    private boolean checkImprovements(ItemStack itemStack, String slot) {
        ItemModular item = (ItemModular) itemStack.getItem();

        if (slot != null) {
            return CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class)
                    .map(module -> {
                        if (hasDisallowedImprovements(module, itemStack)) {
                            return false;
                        }

                        if (improvements.entrySet().stream().allMatch(entry -> entry.getKey().startsWith("!"))) {
                            return true;
                        }

                        return hasImprovements(module, itemStack);
                    })
                    .orElse(false);
        } else {
            boolean hasDisallowed = Arrays.stream(item.getMajorModules(itemStack))
                    .filter(Objects::nonNull)
                    .anyMatch(module -> hasDisallowedImprovements(module, itemStack));

            if (hasDisallowed) {
                return false;
            }

            // if the improvement predicate set only contains disallowed rules we can return true here
            if (improvements.entrySet().stream().allMatch(entry -> entry.getKey().startsWith("!"))) {
                return true;
            }

            return Arrays.stream(item.getMajorModules(itemStack))
                    .filter(Objects::nonNull)
                    .anyMatch(module -> hasImprovements(module, itemStack));
        }
    }

    private boolean hasDisallowedImprovements(ItemModuleMajor module, ItemStack itemStack) {
        ImprovementData[] improvementData = module.getImprovements(itemStack);

        return improvements.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("!"))
                .anyMatch(entry -> {
                    for (ImprovementData data: improvementData) {
                        if (entry.getKey().substring(1).equals(data.key) && (entry.getValue() == -1 || entry.getValue() == data.level)) {
                            return true;
                        }
                    }

                    return false;
                });
    }

    private boolean hasImprovements(ItemModuleMajor module, ItemStack itemStack) {
        ImprovementData[] improvementData = module.getImprovements(itemStack);

        return improvements.entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith("!"))
                .anyMatch(entry -> {
                    for (ImprovementData data: improvementData) {
                        if (entry.getKey().equals(data.key) && (entry.getValue() == -1 || entry.getValue() == data.level)) {
                            return true;
                        }
                    }

                    return false;
                });
    }

    @Override
    public boolean test(ItemStack itemStack) {
       return test(itemStack, null);
    }
}
