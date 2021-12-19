package se.mickelus.tetra.module;

import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.mutil.util.Filter;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.data.MaterialVariantData;
import se.mickelus.tetra.module.data.ModuleData;
import se.mickelus.tetra.module.data.VariantData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class ModuleRegistry {
    private static final Logger logger = LogManager.getLogger();

    public static ModuleRegistry instance;

    private final Map<ResourceLocation, BiFunction<ResourceLocation, ModuleData, ItemModule>> moduleConstructors;
    private Map<ResourceLocation, ItemModule> moduleMap;

    public ModuleRegistry() {
        instance = this;

        moduleConstructors = new HashMap<>();
        moduleMap = Collections.emptyMap();

        DataManager.instance.moduleData.onReload(() -> setupModules(DataManager.instance.moduleData.getData()));
    }

    private void setupModules(Map<ResourceLocation, ModuleData> data) {
        moduleMap = data.entrySet().stream()
                .filter(entry -> validateModuleData(entry.getKey(), entry.getValue()))
                .flatMap(entry -> expandEntry(entry).stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> setupModule(entry.getKey(), entry.getValue())
                ));
    }

    private boolean validateModuleData(ResourceLocation identifier, ModuleData data) {
        if (data == null) {
            logger.warn("Failed to create module from module data '{}': Data is null (probably due to it failing to parse)",
                    identifier);
            return false;
        }

        if (!moduleConstructors.containsKey(data.type)) {
            logger.warn("Failed to create module from module data '{}': Unknown type '{}'", identifier, data.type);
            return false;
        }

        if (data.slots == null || data.slots.length < 1) {
            logger.warn("Failed to create module from module data '{}': Slots field is empty",
                    identifier);
            return false;
        }

        return true;
    }

    // todo: hacky stuff to get multislot modules to work, there has to be another way
    private Collection<Pair<ResourceLocation, ModuleData>> expandEntry(Map.Entry<ResourceLocation, ModuleData> entry) {
        ModuleData moduleData = entry.getValue();
        if (moduleData.slotSuffixes.length > 0) {
            ArrayList<Pair<ResourceLocation, ModuleData>> result = new ArrayList<>(moduleData.slots.length);
            for (int i = 0; i < moduleData.slots.length; i++) {
                ModuleData dataCopy = moduleData.shallowCopy();
                dataCopy.slots = new String[]{moduleData.slots[i]};
                dataCopy.slotSuffixes = new String[]{moduleData.slotSuffixes[i]};

                ResourceLocation suffixedIdentifier = new ResourceLocation(
                        entry.getKey().getNamespace(),
                        entry.getKey().getPath() + moduleData.slotSuffixes[i]);

                result.add(new ImmutablePair<>(suffixedIdentifier, dataCopy));
            }

            return result;
        }
        return Collections.singletonList(new ImmutablePair<>(entry.getKey(), entry.getValue()));
    }

    /**
     * Expands all material based variants for this module data.
     *
     * @param moduleData
     */
    private void expandMaterialVariants(ModuleData moduleData) {
        moduleData.variants = Arrays.stream(moduleData.variants)
                .flatMap(variant ->
                        variant instanceof MaterialVariantData
                                ? expandMaterialVariant((MaterialVariantData) variant)
                                : Stream.of(variant))
                .toArray(VariantData[]::new);
    }

    private Stream<VariantData> expandMaterialVariant(MaterialVariantData source) {
        return Arrays.stream(source.materials)
                .map(rl -> rl.getPath().endsWith("/")
                        ? DataManager.instance.materialData.getDataIn(rl)
                        : Optional.ofNullable(DataManager.instance.materialData.getData(rl)).map(Collections::singletonList).orElseGet(Collections::emptyList))
                .flatMap(Collection::stream)
                .map(source::combine);
    }

    private void handleVariantDuplicates(ModuleData data) {
        // todo: merge variant data instead of discarding duplicates
        data.variants = Arrays.stream(data.variants)
                .filter(Filter.distinct(variant -> variant.key))
                .toArray(VariantData[]::new);
    }

    private ItemModule setupModule(ResourceLocation identifier, ModuleData data) {
        expandMaterialVariants(data);
        handleVariantDuplicates(data);

        return moduleConstructors.get(data.type).apply(identifier, data);
    }

    public void registerModuleType(ResourceLocation identifier, BiFunction<ResourceLocation, ModuleData, ItemModule> constructor) {
        moduleConstructors.put(identifier, constructor);
    }


    public ItemModule getModule(ResourceLocation identifier) {
        return moduleMap.get(identifier);
    }

    public Collection<ItemModule> getAllModules() {
        return moduleMap.values();
    }
}
