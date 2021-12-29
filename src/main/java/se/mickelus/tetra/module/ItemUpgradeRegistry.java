package se.mickelus.tetra.module;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.modular.IModularItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class ItemUpgradeRegistry {
    private static final Logger logger = LogManager.getLogger();

    public static ItemUpgradeRegistry instance;

    private final List<BiFunction<ItemStack, ItemStack, ItemStack>> replacementHooks;
    private List<ReplacementDefinition> replacementDefinitions;

    public ItemUpgradeRegistry() {
        instance = this;
        replacementHooks = new ArrayList<>();

        replacementDefinitions = Collections.emptyList();
        DataManager.instance.replacementData.onReload(() -> {
            replacementDefinitions = DataManager.instance.replacementData.getData().values().stream()
                    .flatMap(Arrays::stream)
                    .filter(replacementDefinition -> replacementDefinition.predicate != null)
                    .collect(Collectors.toList());
        });
    }

    /**
     * Register a hook that will be run for every item that is converted into a tetra item
     *
     * @param hook Bi-function where the first itemstack is the original itemstack and the second is the replacement stack, the returned value will
     *             override the replacement stack
     */
    public void registerReplacementHook(BiFunction<ItemStack, ItemStack, ItemStack> hook) {
        replacementHooks.add(hook);
    }

    /**
     * Attempts to get a modular itemstack to replace the given non-modular itemstack.
     * Make sure to call {@link IModularItem#updateIdentifier} on the new item afterwards to make rendering cheaper.
     *
     * @param itemStack A non-modular itemstack
     * @return The modular counterpart to the given item, or an empty itemstack if there is none
     */
    public ItemStack getReplacement(ItemStack itemStack) {
        for (ReplacementDefinition replacementDefinition : replacementDefinitions) {
            if (replacementDefinition.predicate.matches(itemStack)) {
                ItemStack replacementStack = replacementDefinition.itemStack.copy();

                replacementStack.setDamageValue(itemStack.getDamageValue());

                for (BiFunction<ItemStack, ItemStack, ItemStack> hook : replacementHooks) {
                    replacementStack = hook.apply(itemStack, replacementStack);
                }

                return replacementStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemModule getModule(String key) {
        return ModuleRegistry.instance.getModule(new ResourceLocation(TetraMod.MOD_ID, key));
    }

    public Collection<ItemModule> getAllModules() {
        return ModuleRegistry.instance.getAllModules();
    }
}
