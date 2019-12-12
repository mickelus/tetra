package se.mickelus.tetra.module.schema;

import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.advancements.ImprovementCraftCriterion;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.data.EnchantmentMapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class BookEnchantSchema implements UpgradeSchema {
    private static final String key = "book_enchant";

    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";
    private static final String slotSuffix = ".slot1";

    protected ItemModuleMajor module;

    private GlyphData glyph = new GlyphData(GuiTextures.workbench, 80, 32);

    public BookEnchantSchema(ItemModuleMajor module) {
        this.module = module;

        ItemUpgradeRegistry.instance.registerSchema(this);
    }

    @Override
    public String getKey() {
        return key + "/" + module.getKey();
    }

    @Override
    public String getName() {
        return I18n.format(key + nameSuffix);
    }

    @Override
    public String getDescription(ItemStack itemStack) {
        return I18n.format(key + descriptionSuffix);
    }

    @Override
    public int getNumMaterialSlots() {
        return 1;
    }

    @Override
    public String getSlotName(final ItemStack itemStack, final int index) {
        return I18n.format(key + slotSuffix);
    }

    @Override
    public int getRequiredQuantity(ItemStack itemStack, int index, ItemStack materialStack) {
        return 1;
    }

    @Override
    public boolean acceptsMaterial(ItemStack itemStack, int index, ItemStack materialStack) {
        return !materialStack.isEmpty() && materialStack.getItem() instanceof EnchantedBookItem &&
                EnchantmentHelper.getEnchantments(materialStack).entrySet().stream()
                .anyMatch(entry -> {
                    return Arrays.stream(ItemUpgradeRegistry.instance.getEnchantmentMappings(entry.getKey()))
                            .anyMatch(mapping ->
                                    module.acceptsImprovementLevel(mapping.improvement, (int) (entry.getValue() / mapping.multiplier)));
                });
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, ItemStack[] materials) {
        return acceptsMaterial(itemStack, 0, materials[0]);
    }

    @Override
    public boolean isApplicableForItem(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) itemStack.getItem();
            return item.hasModule(itemStack, module);
        }
        return false;
    }

    @Override
    public boolean isApplicableForSlot(String slot, ItemStack targetStack) {
        return module.getSlot().equals(slot);
    }

    @Override
    public boolean canApplyUpgrade(PlayerEntity player, ItemStack itemStack, ItemStack[] materials, String slot, int[] availableCapabilities) {
        return isMaterialsValid(itemStack, materials) && player.experienceLevel >= getExperienceCost(itemStack, materials, slot);
    }

    @Override
    public boolean isIntegrityViolation(PlayerEntity player, ItemStack itemStack, ItemStack[] materials, String slot) {
        return false;
    }

    @Override
    public ItemStack applyUpgrade(ItemStack itemStack, ItemStack[] materials, boolean consumeMaterials, String slot, PlayerEntity player) {
        ItemStack upgradedStack = itemStack.copy();

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(materials[0]);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            for (EnchantmentMapping mapping : ItemUpgradeRegistry.instance.getEnchantmentMappings(entry.getKey())) {
                if (module.acceptsImprovementLevel(mapping.improvement, entry.getValue())) {
                    module.addImprovement(upgradedStack, mapping.improvement, entry.getValue());

                    if (consumeMaterials && player instanceof ServerPlayerEntity) {
                        ImprovementCraftCriterion.trigger((ServerPlayerEntity) player, itemStack, upgradedStack, getKey(), slot,
                                mapping.improvement, (int) (entry.getValue() / mapping.multiplier), null, -1);
                    }
                }
            }
        }

        if (consumeMaterials) {
            materials[0].shrink(1);
        }

        return upgradedStack;
    }

    @Override
    public boolean checkCapabilities(ItemStack targetStack, ItemStack[] materials, int[] availableCapabilities) {
        return true;
    }

    @Override
    public Collection<Capability> getRequiredCapabilities(ItemStack targetStack, ItemStack[] materials) {
        return Collections.emptyList();
    }

    @Override
    public int getRequiredCapabilityLevel(ItemStack targetStack, ItemStack[] materials, Capability capability) {
        return 0;
    }

    @Override
    public int getExperienceCost(ItemStack targetStack, ItemStack[] materials, String slot) {
        int cost = 0;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(materials[0]);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            for (EnchantmentMapping mapping : ItemUpgradeRegistry.instance.getEnchantmentMappings(entry.getKey())) {
                if (module.acceptsImprovementLevel(mapping.improvement, entry.getValue())) {
                    cost += entry.getValue() / mapping.multiplier;
                }
            }
        }

        int capacityPenalty = Math.max(0, -module.getMagicCapacity(targetStack));

        return cost + capacityPenalty;
    }

    @Override
    public SchemaType getType() {
        return SchemaType.improvement;
    }

    @Override
    public GlyphData getGlyph() {
        return glyph;
    }

    @Override
    public OutcomePreview[] getPreviews(ItemStack targetStack, String slot) {
        return new OutcomePreview[0];
    }
}
