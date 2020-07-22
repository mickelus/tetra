package se.mickelus.tetra.module.schema;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

public class RepairSchema extends BaseSchema {
    private static final String localizationPrefix = TetraMod.MOD_ID + "/schema/";
    private static final String nameSuffix = ".name";
    private static final String slotSuffix = ".slot1";
    private static final String descriptionSuffix = ".description";
    private static final String extendedDescriptionSuffix = ".description_details";

    private String key = "repair";

    private ModularItem item;

    private GlyphData glyph = new GlyphData(GuiTextures.workbench, 0, 52);

    public RepairSchema(ModularItem item) {
        this.item = item;
    }

    public String getSlot(ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .map(item -> item.getRepairSlot(itemStack))
                .orElse(null);
    }

    @Override
    public String getKey() {
        return key + "/" + item.getRegistryName().getPath();
    }

    @Override
    public String getName() {
        return I18n.format(localizationPrefix + key + nameSuffix);
    }

    @Override
    public String getDescription(@Nullable ItemStack itemStack) {
        if (itemStack != null) {
            return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                    .map(item -> I18n.format(localizationPrefix + key + extendedDescriptionSuffix,
                            item.getRepairModuleName(itemStack)))
                    .orElse(I18n.format(localizationPrefix + key + descriptionSuffix));
        }

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
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .map(item -> item.getRepairDefinitions(itemStack))
                .map(Collection::stream)
                .orElse(Stream.empty())
                .map(definition -> definition.material)
                .map(Material::getApplicableItemStacks)
                .flatMap(Arrays::stream)
                .toArray(ItemStack[]::new);
    }

    @Override
    public int getRequiredQuantity(ItemStack itemStack, int index, ItemStack materialStack) {
        if (index == 0 && itemStack.getItem() instanceof ModularItem) {
            ModularItem item = (ModularItem) itemStack.getItem();
            return item.getRepairMaterialCount(itemStack, materialStack);
        }
        return 0;
    }

    @Override
    public boolean acceptsMaterial(final ItemStack itemStack, String itemSlot, final int index, final ItemStack materialStack) {
        if (index == 0 && itemStack.getItem() instanceof ModularItem) {
            return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                    .map(item -> item.getRepairDefinitions(itemStack))
                    .map(Collection::stream)
                    .orElse(Stream.empty())
                    .map(definition -> definition.material.predicate)
                    .anyMatch(predicate -> predicate.test(materialStack));
        }
        return false;
    }

    @Override
    public boolean isApplicableForItem(ItemStack itemStack) {
        return item.getClass().isInstance(itemStack.getItem());
    }

    @Override
    public boolean isApplicableForSlot(String slot, ItemStack targetStack) {
        return slot == null;
    }

    @Override
    public ItemStack applyUpgrade(final ItemStack itemStack, final ItemStack[] materials, boolean consumeMaterials, String slot, PlayerEntity player) {
        ItemStack upgradedStack = itemStack.copy();
        ModularItem item = (ModularItem) upgradedStack.getItem();
        int quantity = getRequiredQuantity(itemStack, 0, materials[0]);

        item.repair(upgradedStack);

        if (consumeMaterials) {
            materials[0].shrink(quantity);
        }

        return upgradedStack;
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, String itemSlot, ItemStack[] materials) {
        return acceptsMaterial(itemStack, itemSlot, 0, materials[0])
                && materials[0].getCount() >= getRequiredQuantity(itemStack, 0, materials[0]);
    }

    @Override
    public boolean isIntegrityViolation(PlayerEntity player, ItemStack itemStack, final ItemStack[] materials, String slot) {
        return false;
    }

    @Override
    public Collection<Capability> getRequiredCapabilities(final ItemStack targetStack, final ItemStack[] materials) {
        if (targetStack.getItem() instanceof ModularItem) {
            ModularItem item = (ModularItem) targetStack.getItem();
            return item.getRepairRequiredCapabilities(targetStack, materials[0]);
        }
        return Collections.emptyList();
    }

    @Override
    public int getRequiredCapabilityLevel(final ItemStack targetStack, final ItemStack[] materials, Capability capability) {
        if (targetStack.getItem() instanceof ModularItem) {
            ModularItem item = (ModularItem) targetStack.getItem();
            return item.getRepairRequiredCapabilityLevel(targetStack, materials[0], capability);
        }
        return 0;
    }

    @Override
    public int getExperienceCost(ItemStack targetStack, ItemStack[] materials, String slot) {
        if (targetStack.getItem() instanceof ModularItem) {
            ModularItem item = (ModularItem) targetStack.getItem();
            return item.getRepairRequiredExperience(targetStack);
        }
        return 0;
    }

    @Override
    public SchemaType getType() {
        return SchemaType.other;
    }

    @Override
    public GlyphData getGlyph() {
        return glyph;
    }
}
