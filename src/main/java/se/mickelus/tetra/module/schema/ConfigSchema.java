package se.mickelus.tetra.module.schema;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.GlyphData;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConfigSchema implements UpgradeSchema {

    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";
    private static final String slotSuffix = ".slot";

    private SchemaDefinition definition;

    public ConfigSchema(SchemaDefinition definition) {
        this.definition = definition;

        String[] faultyModuleOutcomes = Arrays.stream(definition.outcomes)
                .map(outcome -> outcome.moduleKey)
                .filter(Objects::nonNull)
                .filter(moduleKey -> ItemUpgradeRegistry.instance.getModule(moduleKey) == null)
                .toArray(String[]::new);

        if (faultyModuleOutcomes.length == 0) {
            ItemUpgradeRegistry.instance.registerSchema(this);
        } else {
            System.err.println(String.format("Skipping schema '%s' due to faulty module keys:", definition.key));
            for (String faultyKey : faultyModuleOutcomes) {
                System.err.println("\t" + faultyKey);
            }
        }
    }

    private Optional<OutcomeDefinition> getOutcomeFromMaterial(ItemStack materialStack, int slot) {
        return Arrays.stream(definition.outcomes)
                .filter(outcome -> outcome.materialSlot == slot)
                .filter(outcome -> outcome.material.craftPredicate.test(materialStack))
                .findAny();
    }

    @Override
    public String getKey() {
        return definition.key;
    }

    @Override
    public String getName() {
        return I18n.format(definition.key + nameSuffix);
    }

    @Override
    public String getDescription() {
        return I18n.format(definition.key + descriptionSuffix);
    }

    @Override
    public int getNumMaterialSlots() {
        return definition.materialSlotCount;
    }

    @Override
    public String getSlotName(ItemStack itemStack, int index) {
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
        for (int i = 0; i < materials.length; i++) {
            if (i < definition.materialSlotCount && !acceptsMaterial(itemStack, i, materials[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canUpgrade(ItemStack itemStack) {
        return definition.requirement.test(itemStack);
    }

    @Override
    public boolean isApplicableForSlot(String slot) {
        return Arrays.stream(definition.slots)
                .anyMatch(s -> s.equals(slot));
    }

    @Override
    public boolean canApplyUpgrade(EntityPlayer player, ItemStack itemStack, ItemStack[] materials, String slot) {
        return isMaterialsValid(itemStack, materials)
                && !isIntegrityViolation(player, itemStack, materials, slot)
                && checkCapabilities(player, itemStack, materials);
    }

    @Override
    public boolean isIntegrityViolation(EntityPlayer player, ItemStack itemStack, ItemStack[] materials, String slot) {
        ItemStack upgradedStack = applyUpgrade(itemStack, materials, false, slot, null);
        return ItemModular.getIntegrityGain(upgradedStack) + ItemModular.getIntegrityCost(upgradedStack) < 0;
    }

    @Override
    public boolean checkCapabilities(EntityPlayer player, ItemStack targetStack, ItemStack[] materials) {
        return getRequiredCapabilities(targetStack, materials).stream()
                .allMatch(capability -> CapabilityHelper.getCapabilityLevel(player, capability) >= getRequiredCapabilityLevel(targetStack, materials, capability));
    }

    @Override
    public Collection<Capability> getRequiredCapabilities(ItemStack targetStack, ItemStack[] materials) {
        return IntStream.range(0, materials.length)
                .mapToObj(index -> getOutcomeFromMaterial(materials[index], index))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(outcome -> outcome.requiredCapabilities.getValues().stream())
                .distinct()
                .collect(Collectors.toSet());
    }

    @Override
    public int getRequiredCapabilityLevel(ItemStack targetStack, ItemStack[] materials, Capability capability) {
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

    }

    @Override
    public ItemStack applyUpgrade(ItemStack itemStack, ItemStack[] materials, boolean consumeMaterials, String slot, EntityPlayer player) {
        ItemStack upgradedStack = itemStack.copy();

        for (int i = 0; i < materials.length; i++) {
            final int index = i;
            Optional<OutcomeDefinition> outcomeOptional = getOutcomeFromMaterial(materials[index], index);
            outcomeOptional.ifPresent(outcome -> {
                if (outcome.moduleKey != null) {
                    ItemModule module = ItemUpgradeRegistry.instance.getModule(outcome.moduleKey);
                    ItemModule previousModule = removePreviousModule(upgradedStack, module.getSlot());
                    module.addModule(upgradedStack, outcome.moduleVariant, player);
                    if (previousModule != null && consumeMaterials) {
                        previousModule.postRemove(upgradedStack, player);
                    }
                } else {
                    outcome.improvements.forEach((key, value) -> ItemModuleMajor.addImprovement(upgradedStack, slot, key, value));
                }

                if (consumeMaterials) {
                    if (player instanceof EntityPlayerMP) {
                        CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP) player, upgradedStack);
                    }
                    materials[index].shrink(outcome.material.count);
                }
            });
        }
        return upgradedStack;
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
    public GlyphData getGlyph() {
        return definition.glyph;
    }
}
