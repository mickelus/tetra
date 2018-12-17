package se.mickelus.tetra.module;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.data.DataHandler;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.schema.*;

import java.util.*;
import java.util.function.Function;

public class ItemUpgradeRegistry {

    public static ItemUpgradeRegistry instance;

    private List<Function<ItemStack, ItemStack>> replacementFunctions;
    private List<ReplacementDefinition> replacementDefinitions;

    private Map<String, UpgradeSchema> schemaMap;
    private Map<String, RepairDefinition> repairMap;

    private Map<String, ItemModule> moduleMap;

    public ItemUpgradeRegistry() {
        instance = this;
        replacementFunctions = new ArrayList<> ();
        replacementDefinitions = new ArrayList<> ();
        schemaMap = new HashMap<>();
        repairMap = new HashMap<>();
        moduleMap = new HashMap<>();
    }

    public UpgradeSchema[] getAvailableSchemas(EntityPlayer player, ItemStack itemStack) {
        return schemaMap.values().stream()
                .filter(upgradeSchema -> playerHasSchema(player, upgradeSchema))
                .filter(upgradeSchema -> upgradeSchema.isApplicableForItem(itemStack))
                .toArray(UpgradeSchema[]::new);
    }

    public UpgradeSchema getSchema(String key) {
        return schemaMap.get(key);
    }

    public boolean playerHasSchema(EntityPlayer player, UpgradeSchema schema) {
        return true;
    }

    public void registerSchema(UpgradeSchema upgradeSchema) {
        schemaMap.put(upgradeSchema.getKey(), upgradeSchema);
    }

    public void registerConfigSchema(String path) {
        for (SchemaDefinition definition : DataHandler.instance.getSchemaDefinitions(path)) {
            if (definition.slots.length == definition.keySuffixes.length) {
                for (int i = 0; i < definition.slots.length; i++) {
                    try {
                        registerConfigSchema(definition, new ConfigSchema(definition, definition.keySuffixes[i], definition.slots[i]));
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
        }
    }

    private void registerConfigSchema(SchemaDefinition definition, ConfigSchema schema) throws InvalidSchemaException {
        registerSchema(schema);

        if (definition.repair) {
            for (OutcomeDefinition outcomeDefinition: definition.outcomes) {
                if (!outcomeDefinition.material.repairMaterial.isEmpty() && outcomeDefinition.moduleVariant != null) {
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

    public void registerReplacementDefinition(String path) {
        Collections.addAll(replacementDefinitions, DataHandler.instance.getReplacementDefinition(path));
    }

    public void registerReplacementFunction(Function<ItemStack, ItemStack> replacementFunction) {
        replacementFunctions.add(replacementFunction);
    }

    public ItemStack getReplacement(ItemStack itemStack) {
        for (ReplacementDefinition replacementDefinition: replacementDefinitions) {
            if (replacementDefinition.predicate.test(itemStack)) {
                ItemStack replacementStack = replacementDefinition.itemStack.copy();
                replacementStack.setItemDamage(itemStack.getItemDamage());
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
                String improvement = ItemUpgradeRegistry.instance.getImprovementFromEnchantment(entry.getKey());
                ItemModuleMajor[] modules = Arrays.stream(item.getMajorModules(modularStack))
                        .filter(module -> module.acceptsImprovement(improvement))
                        .toArray(ItemModuleMajor[]::new);
                if (modules.length > 0) {
                    float level = 1f * entry.getValue() / modules.length;

                    for (int i = 0; i < modules.length; i++) {
                        if (i == 0) {
                            modules[i].addImprovement(modularStack, improvement, (int) Math.ceil(level));
                        } else {
                            modules[i].addImprovement(modularStack, improvement, (int) level);
                        }
                    }
                }
            }
        }
    }

    public void registerModule(String key, ItemModule module) {
        moduleMap.put(key, module);
    }

    public ItemModule getModule(String key) {
        return moduleMap.get(key);
    }

    public Collection<ItemModule> getAllModules() {
	    return moduleMap.values();
    }

    public String getImprovementFromEnchantment(Enchantment enchantment) {
        return Optional.ofNullable(enchantment.getRegistryName())
                .map(ResourceLocation::getResourcePath)
                .map(path -> "enchantment/" + path)
                .orElse(null);
    }
}
