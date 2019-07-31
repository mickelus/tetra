package se.mickelus.tetra.module.schema;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.advancements.ImprovementCraftCriterion;
import se.mickelus.tetra.advancements.ModuleCraftCriterion;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.ItemPredicateModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.GlyphData;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConfigSchema extends BaseSchema {

    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";
    private static final String slotSuffix = ".slot";

    private SchemaDefinition definition;

    private String keySuffix;
    private String moduleSlot;

    public ConfigSchema(SchemaDefinition definition) throws InvalidSchemaException {
        this(definition, "", null);
    }

    public ConfigSchema(SchemaDefinition definition, String keySuffix, String moduleSlot) throws InvalidSchemaException {
        this.definition = definition;
        this.keySuffix = keySuffix;
        this.moduleSlot = moduleSlot;

        String[] faultyModuleOutcomes = Arrays.stream(definition.outcomes)
                .map(this::getModuleKey)
                .filter(Objects::nonNull)
                .filter(moduleKey -> ItemUpgradeRegistry.instance.getModule(moduleKey) == null)
                .toArray(String[]::new);

        if (faultyModuleOutcomes.length != 0) {
            throw new InvalidSchemaException(definition.key, faultyModuleOutcomes);
        }
    }

    private String getModuleKey(OutcomeDefinition outcome) {
        if (outcome.moduleKey != null) {
            return outcome.moduleKey + keySuffix;
        }
        return null;
    }

    private Optional<OutcomeDefinition> getOutcomeFromMaterial(ItemStack materialStack, int slot) {
        return Arrays.stream(definition.outcomes)
                .filter(outcome -> outcome.materialSlot == slot)
                .filter(outcome -> outcome.material.predicate != null && outcome.material.predicate.test(materialStack))
                .findAny();
    }

    @Override
    public String getKey() {
        return definition.key + keySuffix;
    }

    @Override
    public String getName() {
        if (definition.localizationKey != null) {
            return I18n.format(definition.localizationKey + nameSuffix);
        }
        return I18n.format(definition.key + nameSuffix);
    }

    @Override
    public String getDescription() {
        if (definition.localizationKey != null) {
            return I18n.format(definition.localizationKey + descriptionSuffix);
        }
        return I18n.format(definition.key + descriptionSuffix);
    }

    @Override
    public int getNumMaterialSlots() {
        return definition.materialSlotCount;
    }

    @Override
    public String getSlotName(ItemStack itemStack, int index) {
        if (definition.localizationKey != null) {
            return I18n.format(definition.localizationKey + slotSuffix + (index + 1));
        }
        return I18n.format(definition.key + slotSuffix + (index + 1));
    }

    @Override
    public int getRequiredQuantity(ItemStack itemStack, int index, ItemStack materialStack) {
        return getOutcomeFromMaterial(materialStack, index)
                .map(outcome -> outcome.material.count)
                .orElse(0);
    }

    @Override
    public boolean acceptsMaterial(ItemStack itemStack, int index, ItemStack materialStack) {
        return getOutcomeFromMaterial(materialStack, index).isPresent();
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, ItemStack[] materials) {
        if (getNumMaterialSlots() == 0) {
            return true;
        }

        if (materials.length < definition.materialSlotCount) {
            return false;
        }

        for (int i = 0; i < definition.materialSlotCount; i++) {
            if (!acceptsMaterial(itemStack, i, materials[i])
                    || materials[i].getCount() < getOutcomeFromMaterial(materials[i], i).map(outcome -> outcome.material.count).orElse(0)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isApplicableForItem(ItemStack itemStack) {
        if (definition.hone && (!ConfigHandler.experimentalProgression || !ItemModular.isHoneable(itemStack))) {
            return false;
        }

        if (definition.requirement instanceof ItemPredicateModular) {
            return ((ItemPredicateModular) definition.requirement).test(itemStack, moduleSlot);
        }

        return definition.requirement.test(itemStack);
    }

    @Override
    public boolean isApplicableForSlot(String slot, ItemStack targetStack) {
        if (moduleSlot != null) {
            return moduleSlot.equals(slot);
        }

        return Arrays.stream(definition.slots)
                .anyMatch(s -> s.equals(slot));
    }

    @Override
    public boolean isVisibleForPlayer(EntityPlayer player, ItemStack targetStack) {
        if (definition.materialRevealSlot > -1) {
            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 4; y++) {
                    if (acceptsMaterial(targetStack, definition.materialRevealSlot, player.inventory.getStackInSlot(y * 9 + x))) {
                        return true;
                    }
                }
            }

            return false;
        }

        return true;
    }

    public boolean isHoning() {
        return definition.hone;
    }

    @Override
    public Collection<Capability> getRequiredCapabilities(ItemStack targetStack, ItemStack[] materials) {
        if (definition.materialSlotCount > 0) {
            return IntStream.range(0, materials.length)
                    .mapToObj(index -> getOutcomeFromMaterial(materials[index], index))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .flatMap(outcome -> outcome.requiredCapabilities.getValues().stream())
                    .distinct()
                    .collect(Collectors.toSet());
        } else {
            return Arrays.stream(definition.outcomes)
                    .findFirst()
                    .map(outcome -> outcome.requiredCapabilities.getValues())
                    .orElseGet(HashSet::new);
        }
    }

    @Override
    public int getRequiredCapabilityLevel(ItemStack targetStack, ItemStack[] materials, Capability capability) {
        if (definition.materialSlotCount > 0) {
            return IntStream.range(0, materials.length)
                    .mapToObj(index -> getOutcomeFromMaterial(materials[index], index))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(outcome -> outcome.requiredCapabilities)
                    .filter(capabilities -> capabilities.contains(capability))
                    .map(capabilities -> capabilities.getLevel(capability))
                    .sorted()
                    .findFirst()
                    .orElse(0);
        } else {
            return Arrays.stream(definition.outcomes)
                    .findFirst()
                    .map(outcome -> outcome.requiredCapabilities)
                    .filter(capabilities -> capabilities.contains(capability))
                    .map(capabilities -> capabilities.getLevel(capability))
                    .orElse(0);
        }

    }

    @Override
    public ItemStack applyUpgrade(ItemStack itemStack, ItemStack[] materials, boolean consumeMaterials, String slot, EntityPlayer player) {
        ItemStack upgradedStack = itemStack.copy();

        if (definition.materialSlotCount > 0) {
            for (int i = 0; i < materials.length; i++) {
                final int index = i;
                Optional<OutcomeDefinition> outcomeOptional = getOutcomeFromMaterial(materials[index], index);
                outcomeOptional.ifPresent(outcome -> {
                    applyOutcome(outcome, upgradedStack, consumeMaterials, slot, player);

                    if (consumeMaterials) {
                        materials[index].shrink(outcome.material.count);

                        triggerAdvancement(outcome, player, itemStack, upgradedStack, slot);
                    }
                });
            }
        } else {
            for (OutcomeDefinition outcome : definition.outcomes) {
                applyOutcome(outcome, upgradedStack, consumeMaterials, slot, player);

                if (consumeMaterials) {
                    triggerAdvancement(outcome, player, itemStack, upgradedStack, slot);
                }
            }
        }

        if (consumeMaterials) {
            if (definition.hone) {
                ItemModular.removeHoneable(upgradedStack);
            }
        }
        return upgradedStack;
    }

    private void applyOutcome(OutcomeDefinition outcome, ItemStack upgradedStack, boolean consumeMaterials, String slot, EntityPlayer player) {
        if (outcome.moduleKey != null) {
            ItemModule module = ItemUpgradeRegistry.instance.getModule(getModuleKey(outcome));
            float durabilityFactor = 0;

            if (upgradedStack.isItemStackDamageable()) {
                durabilityFactor = upgradedStack.getItemDamage() * 1f / upgradedStack.getMaxDamage();
            }

            ItemModule previousModule = removePreviousModule(upgradedStack, module.getSlot());

            module.addModule(upgradedStack, outcome.moduleVariant, player);

            if (upgradedStack.isItemStackDamageable()) {
                upgradedStack.setItemDamage((int) ( durabilityFactor * upgradedStack.getMaxDamage()
                        - ( durabilityFactor * durabilityFactor * module.getDurability(upgradedStack) ) ));
            }

            outcome.improvements.forEach((key, value) -> ItemModuleMajor.addImprovement(upgradedStack, slot, key, value));

            if (previousModule != null && consumeMaterials) {
                previousModule.postRemove(upgradedStack, player);
            }

        } else {
            outcome.improvements.forEach((key, value) -> ItemModuleMajor.addImprovement(upgradedStack, slot, key, value));
        }
    }

    private void triggerAdvancement(OutcomeDefinition outcome, EntityPlayer player, ItemStack itemStack, ItemStack upgradedStack, String slot) {
        if(player instanceof EntityPlayerMP) {

            if (outcome.moduleKey != null) {
                if (outcome.requiredCapabilities.getValues().isEmpty()) {
                    ModuleCraftCriterion.trigger((EntityPlayerMP) player, itemStack, upgradedStack, getKey(), slot, outcome.moduleKey,
                            outcome.moduleVariant, null, -1);
                } else {
                    outcome.requiredCapabilities.valueMap.forEach((capability, capabilityLevel) ->
                            ModuleCraftCriterion.trigger((EntityPlayerMP) player, itemStack, upgradedStack, getKey(), slot, outcome.moduleKey,
                                    outcome.moduleVariant, capability, capabilityLevel));
                }
            }

            outcome.improvements.forEach((improvement, level) -> {
                if (outcome.requiredCapabilities.getValues().isEmpty()) {
                    ImprovementCraftCriterion.trigger((EntityPlayerMP) player, itemStack, upgradedStack, getKey(), slot, improvement, level, null, -1);
                } else {
                    outcome.requiredCapabilities.valueMap.forEach((capability, capabilityLevel) ->
                            ImprovementCraftCriterion.trigger((EntityPlayerMP) player, itemStack, upgradedStack, getKey(), slot, improvement, level,
                            capability, capabilityLevel));
                }
            });
        }
    }

    protected ItemModule removePreviousModule(final ItemStack itemStack, String slot) {
        ItemModular item = (ItemModular) itemStack.getItem();
        ItemModule previousModule = item.getModuleFromSlot(itemStack, slot);
        if (previousModule != null) {
            previousModule.removeModule(itemStack);
        }
        return previousModule;
    }

    @Override
    public SchemaType getType() {
        return definition.displayType;
    }

    @Override
    public SchemaRarity getRarity() {
        return definition.rarity;
    }

    @Override
    public GlyphData getGlyph() {
        return definition.glyph;
    }

    @Override
    public OutcomePreview[] getPreviews(ItemStack targetStack, String slot) {
        return Arrays.stream(definition.outcomes)
                .map(outcome -> {
                    String key = null;
                    GlyphData glyph;

                    if (outcome.moduleKey != null) {
                        ItemModule module = ItemUpgradeRegistry.instance.getModule(getModuleKey(outcome));

                        key = outcome.moduleVariant;
                        glyph = module.getData(outcome.moduleVariant).glyph;
                    } else {
                        if (outcome.improvements.size() == 1) {
                            for (String improvementKey : outcome.improvements.keySet()) {
                                key = improvementKey;
                            }
                            glyph = definition.glyph;
                        } else if (!outcome.improvements.isEmpty()) {
                            key = definition.key;
                            glyph = definition.glyph;
                        } else {
                            return null;
                        }

                    }

                    ItemStack itemStack = targetStack.copy();
                    applyOutcome(outcome, itemStack, false, slot, null);

                    return new OutcomePreview(key, glyph, itemStack, definition.displayType, outcome.requiredCapabilities,
                            outcome.material.getApplicableItemstacks());
                })
                .toArray(OutcomePreview[]::new);
    }
}
