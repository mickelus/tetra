package se.mickelus.tetra.module.schema;

import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.advancements.ImprovementCraftCriterion;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.data.EnchantmentMapping;
import se.mickelus.tetra.util.CastOptional;

import java.util.*;

public class BookEnchantSchema implements UpgradeSchema {
    private static final String localizationPrefix = TetraMod.MOD_ID + "/schema/";
    private static final String key = "book_enchant";

    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";
    private static final String slotSuffix = ".slot1";

    private GlyphData glyph = new GlyphData(GuiTextures.workbench, 80, 32);

    public BookEnchantSchema() {}

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return I18n.format(localizationPrefix + key + nameSuffix);
    }

    @Override
    public String getDescription(ItemStack itemStack) {
        return I18n.format(localizationPrefix + key + descriptionSuffix);
    }

    @Override
    public int getNumMaterialSlots() {
        return 1;
    }

    @Override
    public String getSlotName(final ItemStack itemStack, final int index) {
        return I18n.format(localizationPrefix + key + slotSuffix);
    }

    @Override
    public ItemStack[] getSlotPlaceholders(ItemStack itemStack, int index) {
        return new ItemStack[] {Items.ENCHANTED_BOOK.getDefaultInstance()};
    }

    @Override
    public int getRequiredQuantity(ItemStack itemStack, int index, ItemStack materialStack) {
        return 1;
    }

    @Override
    public boolean acceptsMaterial(ItemStack itemStack, String itemSlot, int index, ItemStack materialStack) {
        ItemModuleMajor module = CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getModuleFromSlot(itemStack, itemSlot))
                .flatMap (mod -> CastOptional.cast(mod, ItemModuleMajor.class))
                .orElse(null);

        return module != null && !materialStack.isEmpty() && materialStack.getItem() instanceof EnchantedBookItem
                && EnchantmentHelper.getEnchantments(materialStack).entrySet().stream()
                .anyMatch(entry -> {
                    return Arrays.stream(ItemUpgradeRegistry.instance.getEnchantmentMappings(entry.getKey()))
                            .anyMatch(mapping ->
                                    module.acceptsImprovementLevel(mapping.improvement, (int) (entry.getValue() / mapping.multiplier)));
                });
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, String itemSlot, ItemStack[] materials) {
        return acceptsMaterial(itemStack, itemSlot, 0, materials[0]);
    }

    @Override
    public boolean isApplicableForItem(ItemStack itemStack) {
        return itemStack.getItem() instanceof ItemModular;
    }

    @Override
    public boolean isApplicableForSlot(String slot, ItemStack targetStack) {
        return CastOptional.cast(targetStack.getItem(), ItemModular.class)
                .map(item -> item.getModuleFromSlot(targetStack, slot))
                .map(module -> module.getMagicCapacityGain(targetStack) > 0)
                .orElse(false);
    }

    @Override
    public boolean canApplyUpgrade(PlayerEntity player, ItemStack itemStack, ItemStack[] materials, String slot, int[] availableCapabilities) {
        return isMaterialsValid(itemStack, slot, materials) && player.experienceLevel >= getExperienceCost(itemStack, materials, slot);
    }

    @Override
    public boolean isIntegrityViolation(PlayerEntity player, ItemStack itemStack, ItemStack[] materials, String slot) {
        return false;
    }

    @Override
    public ItemStack applyUpgrade(ItemStack itemStack, ItemStack[] materials, boolean consumeMaterials, String slot, PlayerEntity player) {
        ItemStack upgradedStack = itemStack.copy();

        ItemModuleMajor module = CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .filter(mod -> mod instanceof ItemModuleMajor)
                .map(mod -> (ItemModuleMajor) mod)
                .orElse(null);

        if (module != null) {
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
        return CastOptional.cast(targetStack.getItem(), ItemModular.class)
                .map(item -> item.getModuleFromSlot(targetStack, slot))
                .filter(module -> module instanceof ItemModuleMajor)
                .map(module -> (ItemModuleMajor) module)
                .map(module -> {
                    int cost = 0;
                    Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(materials[0]);
                    for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                        for (EnchantmentMapping mapping : ItemUpgradeRegistry.instance.getEnchantmentMappings(entry.getKey())) {
                            if (module.acceptsImprovementLevel(mapping.improvement, (int) (entry.getValue() / mapping.multiplier))) {
                                cost += entry.getValue() / mapping.multiplier;
                            }
                        }
                    }

                    int capacityPenalty = Math.max(0, -module.getMagicCapacity(targetStack));

                    return cost + capacityPenalty;
                })
                .orElse(0);
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
