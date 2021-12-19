package se.mickelus.tetra.module;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.data.EnchantmentMapping;

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

    public static void applyEnchantment(IModularItem item, ItemStack itemStack, Enchantment enchantment, int enchantmentLevel) {
        for (EnchantmentMapping mapping : ItemUpgradeRegistry.instance.getEnchantmentMappings(enchantment)) {
            ItemModuleMajor[] modules = Arrays.stream(item.getMajorModules(itemStack))
                    .filter(module -> module.acceptsImprovement(mapping.improvement))
                    .toArray(ItemModuleMajor[]::new);
            if (modules.length > 0) {

                // since efficiency doesn't stack on double headed items it should be fully applied to a single module instead of being split
                if (Enchantments.BLOCK_EFFICIENCY.equals(enchantment)) {
                    float level = 1f * enchantmentLevel / mapping.multiplier;
                    for (ItemModuleMajor module : modules) {
                        if (module.acceptsImprovementLevel(mapping.improvement, (int) level)) {
                            module.addImprovement(itemStack, mapping.improvement, (int) level);
                            break;
                        }
                    }
                } else {
                    float level = 1f * enchantmentLevel / modules.length / mapping.multiplier;

                    for (int i = 0; i < modules.length; i++) {
                        if (i == 0) {
                            if (modules[i].acceptsImprovementLevel(mapping.improvement, (int) Math.ceil(level))) {
                                modules[i].addImprovement(itemStack, mapping.improvement, (int) Math.ceil(level));
                            }
                        } else {
                            if (modules[i].acceptsImprovementLevel(mapping.improvement, (int) level)) {
                                modules[i].addImprovement(itemStack, mapping.improvement, (int) level);
                            }
                        }
                    }

                }
            }
        }
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
                transferEnchantments(itemStack, replacementStack);

                for (BiFunction<ItemStack, ItemStack, ItemStack> hook : replacementHooks) {
                    replacementStack = hook.apply(itemStack, replacementStack);
                }

                return replacementStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private void transferEnchantments(ItemStack sourceStack, ItemStack modularStack) {
        if (modularStack.getItem() instanceof IModularItem) {
            IModularItem item = (IModularItem) modularStack.getItem();
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(sourceStack);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                applyEnchantment(item, modularStack, entry.getKey(), entry.getValue());
            }
        }
    }

    public EnchantmentMapping[] getEnchantmentMappings() {
        return DataManager.instance.enchantmentData.getData().values().stream()
                .flatMap(Arrays::stream)
                .filter(mapping -> mapping.enchantment != null)
                .filter(mapping -> mapping.apply)
                .toArray(EnchantmentMapping[]::new);
    }

    public EnchantmentMapping[] getEnchantmentMappings(String improvement) {
        return DataManager.instance.enchantmentData.getData().values().stream()
                .flatMap(Arrays::stream)
                .filter(mapping -> mapping.enchantment != null)
                .filter(mapping -> improvement.equals(mapping.improvement))
                .filter(mapping -> mapping.apply)
                .toArray(EnchantmentMapping[]::new);
    }

    public EnchantmentMapping[] getEnchantmentMappings(Enchantment enchantment) {
        return DataManager.instance.enchantmentData.getData().values().stream()
                .flatMap(Arrays::stream)
                .filter(mapping -> mapping.enchantment != null)
                .filter(mapping -> enchantment.equals(mapping.enchantment))
                .filter(mapping -> mapping.extract)
                .toArray(EnchantmentMapping[]::new);
    }

    public ItemModule getModule(String key) {
        return ModuleRegistry.instance.getModule(new ResourceLocation(TetraMod.MOD_ID, key));
    }

    public Collection<ItemModule> getAllModules() {
        return ModuleRegistry.instance.getAllModules();
    }
}
