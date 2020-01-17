package se.mickelus.tetra.module;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.data.EnchantmentMapping;
import se.mickelus.tetra.module.schema.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ItemUpgradeRegistry {
    private static final Logger logger = LogManager.getLogger();

    public static ItemUpgradeRegistry instance;

    private List<Function<ItemStack, ItemStack>> replacementFunctions;
    private List<ReplacementDefinition> replacementDefinitions;

    private Map<String, UpgradeSchema> schemaMap;
    private Map<String, RepairDefinition> repairMap;

    private Map<String, ItemModule> moduleMap;

    public ItemUpgradeRegistry() {
        instance = this;
        replacementFunctions = new ArrayList<> ();
        schemaMap = new HashMap<>();
        repairMap = new HashMap<>();
        moduleMap = new HashMap<>();

        replacementDefinitions = Collections.emptyList();
        DataManager.replacementData.onReload(() -> {
            replacementDefinitions = DataManager.replacementData.getData().values().stream()
                    .flatMap(Arrays::stream)
                    .filter(replacementDefinition -> replacementDefinition.predicate != null)
                    .collect(Collectors.toList());
        });

        DataManager.schemaData.onReload(() -> {
            DataManager.schemaData.getData().values().stream()
                    .flatMap(Arrays::stream)
                    .forEach(definition -> {
                        if (definition.slots.length == definition.keySuffixes.length) {
                            for (int i = 0; i < definition.slots.length; i++) {
                                try {
                                    registerConfigSchema(definition,
                                            new ConfigSchema(definition, definition.keySuffixes[i], definition.slots[i]));
                                } catch (InvalidSchemaException e) {
                                    e.printMessage();
                                }
                            }
                        } else {
                            try {
                                registerConfigSchema(definition, new ConfigSchema(definition));
                            } catch (InvalidSchemaException e) {
                                e.printMessage();
                            }
                        }
                    });
        });
    }

    public UpgradeSchema[] getAvailableSchemas(PlayerEntity player, ItemStack itemStack) {
        return schemaMap.values().stream()
                .filter(upgradeSchema -> playerHasSchema(player, itemStack, upgradeSchema))
                .filter(upgradeSchema -> upgradeSchema.isApplicableForItem(itemStack))
                .toArray(UpgradeSchema[]::new);
    }

    public UpgradeSchema[] getSchemas(String slot) {
        return schemaMap.values().stream()
                .filter(upgradeSchema -> upgradeSchema.isApplicableForSlot(slot, ItemStack.EMPTY))
                .toArray(UpgradeSchema[]::new);
    }

    public UpgradeSchema getSchema(String key) {
        return schemaMap.get(key);
    }

    public boolean playerHasSchema(PlayerEntity player, ItemStack targetStack, UpgradeSchema schema) {
        return schema.isVisibleForPlayer(player, targetStack);
    }

    public void registerSchema(UpgradeSchema upgradeSchema) {
        schemaMap.put(upgradeSchema.getKey(), upgradeSchema);
    }

    private void registerConfigSchema(SchemaDefinition definition, ConfigSchema schema) {
        registerSchema(schema);

        if (definition.repair) {
            for (OutcomeDefinition outcomeDefinition: definition.outcomes) {
                if (RepairDefinition.validateOutcome(outcomeDefinition)) {
                    registerRepairDefinition(new RepairDefinition(outcomeDefinition));
                }
            }
        }
    }

    public void registerRepairDefinition(RepairDefinition definition) {
        repairMap.put(definition.moduleVariant, definition);
    }

    public RepairDefinition getRepairDefinition(String moduleVariant) {
        return repairMap.get(moduleVariant);
    }

    public void registerReplacementFunction(Function<ItemStack, ItemStack> replacementFunction) {
        replacementFunctions.add(replacementFunction);
    }

    public ItemStack getReplacement(ItemStack itemStack) {
        for (ReplacementDefinition replacementDefinition: replacementDefinitions) {
            if (replacementDefinition.predicate.test(itemStack)) {
                ItemStack replacementStack = replacementDefinition.itemStack.copy();
                replacementStack.setDamage(itemStack.getDamage());
                transferEnchantments(itemStack, replacementStack);

                return replacementStack;
            }
        }
        for (Function<ItemStack, ItemStack> replacementFunction: replacementFunctions) {
            ItemStack replacementStack = replacementFunction.apply(itemStack);
            if (replacementStack != null) {
                return replacementStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private void transferEnchantments(ItemStack sourceStack, ItemStack modularStack) {
        if (modularStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) modularStack.getItem();
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(sourceStack);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                for (EnchantmentMapping mapping: ItemUpgradeRegistry.instance.getEnchantmentMappings(entry.getKey())) {
                    ItemModuleMajor[] modules = Arrays.stream(item.getMajorModules(modularStack))
                            .filter(module -> module.acceptsImprovement(mapping.improvement))
                            .toArray(ItemModuleMajor[]::new);
                    if (modules.length > 0) {
                        float level = 1f * entry.getValue() / modules.length / mapping.multiplier;

                        for (int i = 0; i < modules.length; i++) {
                            if (i == 0) {
                                if (modules[i].acceptsImprovementLevel(mapping.improvement, (int) Math.ceil(level))) {
                                    modules[i].addImprovement(modularStack, mapping.improvement, (int) Math.ceil(level));
                                }
                            } else {
                                if (modules[i].acceptsImprovementLevel(mapping.improvement, (int) level)) {
                                    modules[i].addImprovement(modularStack, mapping.improvement, (int) level);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public EnchantmentMapping[] getEnchantmentMappings(String improvement) {
        return DataManager.enchantmentData.getData().values().stream()
                .flatMap(Arrays::stream)
                .filter(mapping -> mapping.enchantment != null)
                .filter(mapping -> improvement.equals(mapping.improvement))
                .filter(mapping -> mapping.apply)
                .toArray(EnchantmentMapping[]::new);
    }

    public EnchantmentMapping[] getEnchantmentMappings(Enchantment enchantment) {
        return DataManager.enchantmentData.getData().values().stream()
                .flatMap(Arrays::stream)
                .filter(mapping -> mapping.enchantment != null)
                .filter(mapping -> enchantment.equals(mapping.enchantment))
                .filter(mapping -> mapping.extract)
                .toArray(EnchantmentMapping[]::new);
    }

    public void registerModule(String key, ItemModule module) {
        moduleMap.put(key, module);
    }

    public ItemModule getModule(String key) {
        return ModuleRegistry.instance.getModule(new ResourceLocation(TetraMod.MOD_ID, key));
    }

    public Collection<ItemModule> getAllModules() {
       return ModuleRegistry.instance.getAllModules();
    }
}
