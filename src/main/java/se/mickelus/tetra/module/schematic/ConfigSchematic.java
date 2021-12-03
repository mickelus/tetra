package se.mickelus.tetra.module.schematic;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.advancements.ImprovementCraftCriterion;
import se.mickelus.tetra.advancements.ModuleCraftCriterion;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ItemPredicateModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.data.MaterialMultiplier;
import se.mickelus.tetra.module.data.VariantData;
import se.mickelus.tetra.util.Filter;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
@ParametersAreNonnullByDefault
public class ConfigSchematic extends BaseSchematic {
    private static final String localizationPrefix = TetraMod.MOD_ID + "/schematic/";
    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";
    private static final String slotSuffix = ".slot";

    private SchematicDefinition definition;

    private String keySuffix;
    private String moduleSlot;

    public ConfigSchematic(SchematicDefinition definition) throws InvalidSchematicException {
        this(definition, "", null);
    }

    public ConfigSchematic(SchematicDefinition definition, String keySuffix, String moduleSlot) throws InvalidSchematicException {
        this.definition = definition;
        this.keySuffix = keySuffix;
        this.moduleSlot = moduleSlot;

        String[] faultyModuleOutcomes = Arrays.stream(definition.outcomes)
                .map(this::getModuleKey)
                .filter(Objects::nonNull)
                .filter(moduleKey -> ItemUpgradeRegistry.instance.getModule(moduleKey) == null)
                .toArray(String[]::new);

        if (faultyModuleOutcomes.length != 0) {
            throw new InvalidSchematicException(definition.key, faultyModuleOutcomes);
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
                .filter(outcome -> outcome.material.getPredicate() != null && outcome.material.getPredicate().matches(materialStack))
                .reduce((a, b) -> b); // returns the last element, there's no findLast :c
    }

    @Override
    public String getKey() {
        return definition.key + keySuffix;
    }

    @Override
    public String getName() {
        if (definition.localizationKey != null) {
            return I18n.get(localizationPrefix + definition.localizationKey + nameSuffix);
        }
        return I18n.get(localizationPrefix + definition.key + nameSuffix);
    }

    @Override
    public String getDescription(ItemStack itemStack) {
        if (definition.localizationKey != null) {
            return I18n.get(localizationPrefix + definition.localizationKey + descriptionSuffix);
        }
        return I18n.get(localizationPrefix + definition.key + descriptionSuffix);
    }

    @Nullable
    @Override
    public MaterialMultiplier getMaterialTranslation() {
        return definition.translation;
    }

    @Nullable
    @Override
    public String[] getApplicableMaterials() {
        return definition.applicableMaterials;
    }

    @Override
    public int getNumMaterialSlots() {
        return definition.materialSlotCount;
    }

    @Override
    public String getSlotName(ItemStack itemStack, int index) {
        if (definition.localizationKey != null) {
            return I18n.get(localizationPrefix + definition.localizationKey + slotSuffix + (index + 1));
        }
        return I18n.get(localizationPrefix + definition.key + slotSuffix + (index + 1));
    }

    @Override
    public int getRequiredQuantity(ItemStack itemStack, int index, ItemStack materialStack) {
        return getOutcomeFromMaterial(materialStack, index)
                .map(outcome -> outcome.material.count)
                .orElse(0);
    }

    @Override
    public boolean acceptsMaterial(ItemStack itemStack, String itemSlot, int index, ItemStack materialStack) {
        return getOutcomeFromMaterial(materialStack, index).isPresent();
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, String itemSlot, ItemStack[] materials) {
        if (getNumMaterialSlots() == 0) {
            return true;
        }

        if (materials.length < definition.materialSlotCount) {
            return false;
        }

        for (int i = 0; i < definition.materialSlotCount; i++) {
            if (!acceptsMaterial(itemStack, itemSlot, i, materials[i])
                    || materials[i].getCount() < getOutcomeFromMaterial(materials[i], i).map(outcome -> outcome.material.count).orElse(0)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isApplicableForItem(ItemStack itemStack) {
        if (definition.hone && (!ConfigHandler.moduleProgression.get() || !IModularItem.isHoneable(itemStack))) {
            return false;
        }

        if (definition.requirement instanceof ItemPredicateModular) {
            return ((ItemPredicateModular) definition.requirement).test(itemStack, moduleSlot);
        }

        return definition.requirement.matches(itemStack);
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
    public boolean isVisibleForPlayer(Player player, @Nullable WorkbenchTile tile, ItemStack targetStack) {
        if (definition.locked) {
            return Optional.ofNullable(tile)
                    .map(WorkbenchTile::getUnlockedSchematics)
                    .map(Arrays::stream)
                    .orElseGet(Stream::empty)
                    .anyMatch(rl -> definition.key.startsWith(rl.getPath()));
        }

        if (definition.materialRevealSlot > -1) {
            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 4; y++) {
                    if (getOutcomeFromMaterial(player.getInventory().getItem(y * 9 + x), definition.materialRevealSlot).isPresent()) {
                        return true;
                    }
                }
            }

            return false;
        }

        return true;
    }

    @Override
    public boolean isHoning() {
        return definition.hone;
    }

    @Override
    public Map<ToolAction, Integer> getRequiredToolLevels(ItemStack targetStack, ItemStack[] materials) {
        if (definition.materialSlotCount > 0) {
            return IntStream.range(0, materials.length)
                    .mapToObj(index -> getOutcomeFromMaterial(materials[index], index))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .flatMap(outcome -> outcome.requiredTools.getLevelMap().entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Math::max));
        } else {
            return Arrays.stream(definition.outcomes)
                    .findFirst()
                    .map(outcome -> outcome.requiredTools.getLevelMap())
                    .orElseGet(Collections::emptyMap);
        }
    }

    @Override
    public int getRequiredToolLevel(ItemStack targetStack, ItemStack[] materials, ToolAction toolAction) {
        if (definition.materialSlotCount > 0) {
            return IntStream.range(0, materials.length)
                    .mapToObj(index -> getOutcomeFromMaterial(materials[index], index))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(outcome -> outcome.requiredTools)
                    .filter(tools -> tools.contains(toolAction))
                    .map(tools -> tools.getLevel(toolAction))
                    .sorted()
                    .findFirst()
                    .orElse(0);
        } else {
            return Arrays.stream(definition.outcomes)
                    .findFirst()
                    .map(outcome -> outcome.requiredTools)
                    .filter(tools -> tools.contains(toolAction))
                    .map(tools -> tools.getLevel(toolAction))
                    .orElse(0);
        }

    }

    @Override
    public ItemStack applyUpgrade(ItemStack itemStack, ItemStack[] materials, boolean consumeMaterials, String slot, Player player) {
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

        return upgradedStack;
    }

    private void applyOutcome(OutcomeDefinition outcome, ItemStack upgradedStack, boolean consumeMaterials, String slot, Player player) {
        if (outcome.moduleKey != null) {
            ItemModule module = ItemUpgradeRegistry.instance.getModule(getModuleKey(outcome));

            ItemModule previousModule = removePreviousModule(upgradedStack, module.getSlot());

            module.addModule(upgradedStack, outcome.moduleVariant, player);

            outcome.improvements.forEach((key, value) -> ItemModuleMajor.addImprovement(upgradedStack, slot, key, value));

            if (previousModule != null && consumeMaterials) {
                previousModule.postRemove(upgradedStack, player);
            }

        } else {
            outcome.improvements.forEach((key, value) -> ItemModuleMajor.addImprovement(upgradedStack, slot, key, value));
        }
    }

    private void triggerAdvancement(OutcomeDefinition outcome, Player player, ItemStack itemStack, ItemStack upgradedStack, String slot) {
        if(player instanceof ServerPlayer) {

            if (outcome.moduleKey != null) {
                if (outcome.requiredTools.getValues().isEmpty()) {
                    ModuleCraftCriterion.trigger((ServerPlayer) player, itemStack, upgradedStack, getKey(), slot, outcome.moduleKey,
                            outcome.moduleVariant, null, -1);
                } else {
                    outcome.requiredTools.getLevelMap().forEach((tool, toolLevel) ->
                            ModuleCraftCriterion.trigger((ServerPlayer) player, itemStack, upgradedStack, getKey(), slot, outcome.moduleKey,
                                    outcome.moduleVariant, tool, toolLevel));
                }
            }

            outcome.improvements.forEach((improvement, level) -> {
                if (outcome.requiredTools.getValues().isEmpty()) {
                    ImprovementCraftCriterion.trigger((ServerPlayer) player, itemStack, upgradedStack, getKey(), slot, improvement, level, null, -1);
                } else {
                    outcome.requiredTools.getLevelMap().forEach((tool, toolLevel) ->
                            ImprovementCraftCriterion.trigger((ServerPlayer) player, itemStack, upgradedStack, getKey(), slot, improvement, level,
                            tool, toolLevel));
                }
            });
        }
    }

    @Override
    public boolean willReplace(ItemStack itemStack, ItemStack[] materials, String slot) {
        if (definition.materialSlotCount > 0) {
            for (int i = 0; i < materials.length; i++) {
                Optional<OutcomeDefinition> outcomeOptional = getOutcomeFromMaterial(materials[i], i);
                if (outcomeOptional.isPresent() && outcomeOptional.get().moduleKey != null) {
                    return true;
                }
            }
        } else {
            for (OutcomeDefinition outcome : definition.outcomes) {
                if (outcome.moduleKey != null) {
                    return true;
                }
            }
        }

        return false;
    }

    protected ItemModule removePreviousModule(final ItemStack itemStack, String slot) {
        IModularItem item = (IModularItem) itemStack.getItem();
        ItemModule previousModule = item.getModuleFromSlot(itemStack, slot);
        if (previousModule != null) {
            previousModule.removeModule(itemStack);
        }
        return previousModule;
    }

    @Override
    public int getExperienceCost(ItemStack targetStack, ItemStack[] materials, String slot) {
        int cost = 0;
        if (definition.materialSlotCount > 0) {
            for (int i = 0; i < materials.length; i++) {
                cost += getOutcomeFromMaterial(materials[i], i)
                        .map(outcome -> outcome.experienceCost)
                        .orElse(0);
            }
        } else {
            cost += Arrays.stream(definition.outcomes)
                    .mapToInt(outcome -> outcome.experienceCost)
                    .sum();
        }

        return cost;
    }

    @Override
    public SchematicType getType() {
        return definition.displayType;
    }

    @Override
    public SchematicRarity getRarity() {
        return definition.rarity;
    }

    @Override
    public GlyphData getGlyph() {
        return definition.glyph;
    }

    @Override
    public OutcomePreview[] getPreviews(ItemStack targetStack, String slot) {
        return Arrays.stream(definition.outcomes)
                .filter(outcome -> !outcome.hidden)
                .map(outcome -> {
                    ItemStack itemStack = targetStack.copy();

                    String key = null;
                    String name = "";
                    String category = "misc";
                    int level = -1;
                    GlyphData glyph;

                    applyOutcome(outcome, itemStack, false, slot, null);

                    if (outcome.moduleKey != null) {
                        VariantData variant = ItemUpgradeRegistry.instance.getModule(getModuleKey(outcome)).getVariantData(outcome.moduleVariant);

                        key = outcome.moduleVariant;
                        name = getVariantName(outcome, itemStack);
                        glyph = variant.glyph;
                        category = variant.category;
                    } else {
                        if (outcome.improvements.size() == 1) {
                            for (Map.Entry<String, Integer> entry: outcome.improvements.entrySet()) {
                                key = entry.getKey();
                                name = IModularItem.getImprovementName(key, entry.getValue());
                                level = entry.getValue();
                            }
                            glyph = definition.glyph;
                        } else if (!outcome.improvements.isEmpty()) {
                            key = definition.key;
                            glyph = definition.glyph;
                        } else {
                            return null;
                        }
                    }

                    return new OutcomePreview(outcome.moduleKey, key, name, category, level, glyph, itemStack, definition.displayType, outcome.requiredTools,
                            outcome.material.getApplicableItemStacks());
                })
                .filter(Filter.distinct(preview -> preview.variantKey))
                .toArray(OutcomePreview[]::new);
    }

    private String getVariantName(OutcomeDefinition outcome, ItemStack itemStack) {
        return Optional.ofNullable(getModuleKey(outcome))
                .map(ItemUpgradeRegistry.instance::getModule)
                .map(module -> module.getName(itemStack))
                .orElse("");
    }
}
