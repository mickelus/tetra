package se.mickelus.tetra.module.schema;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

public class RepairSchema extends BaseSchema {
    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";
    private static final String extendedDescriptionSuffix = ".description_details";

    private String key = "repair_schema";

    private ItemModular item;

    private GlyphData glyph = new GlyphData("textures/gui/workbench.png", 0, 52);

    public RepairSchema(ItemModular item) {
        this.item = item;
        ItemUpgradeRegistry.instance.registerSchema(this);
    }

    public String getSlot(ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getRepairSlot(itemStack))
                .orElse(null);
    }

    @Override
    public String getKey() {
        return key + "/" + item.getUnlocalizedName();
    }

    @Override
    public String getName() {
        return I18n.format(key + nameSuffix);
    }

    @Override
    public String getDescription(@Nullable ItemStack itemStack) {
        if (itemStack != null) {
            return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                    .map(item -> I18n.format(key + extendedDescriptionSuffix,
                            item.getRepairModuleName(itemStack),
                            item.getRepairAmount(itemStack)))
                    .orElse(I18n.format(key + descriptionSuffix));
        }

        return I18n.format(key + descriptionSuffix);
    }

    @Override
    public int getNumMaterialSlots() {
        return 1;
    }

    @Override
    public String getSlotName(final ItemStack itemStack, final int index) {
        if (itemStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) itemStack.getItem();
            return item.getRepairMaterial(itemStack).getDisplayName();
        }
        return "?";
    }

    @Override
    public int getRequiredQuantity(ItemStack itemStack, int index, ItemStack materialStack) {
        if (index == 0 && itemStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) itemStack.getItem();
            return item.getRepairMaterialCount(itemStack);
        }
        return 0;
    }

    @Override
    public boolean acceptsMaterial(final ItemStack itemStack, final int index, final ItemStack materialStack) {
        if (index == 0 && itemStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) itemStack.getItem();
            return item.getRepairMaterial(itemStack).predicate.test(materialStack);
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
        ItemModular item = (ItemModular) upgradedStack.getItem();
        int quantity = getRequiredQuantity(itemStack, 0, materials[0]);

        item.repair(upgradedStack);

        if (consumeMaterials) {
            materials[0].shrink(quantity);
        }

        return upgradedStack;
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, ItemStack[] materials) {
        return acceptsMaterial(itemStack, 0, materials[0])
                && materials[0].getCount() >= getRequiredQuantity(itemStack, 0, materials[0]);
    }

    @Override
    public boolean isIntegrityViolation(PlayerEntity player, ItemStack itemStack, final ItemStack[] materials, String slot) {
        return false;
    }

    @Override
    public Collection<Capability> getRequiredCapabilities(final ItemStack targetStack, final ItemStack[] materials) {
        if (targetStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) targetStack.getItem();
            return item.getRepairRequiredCapabilities(targetStack);
        }
        return Collections.emptyList();
    }

    @Override
    public int getRequiredCapabilityLevel(final ItemStack targetStack, final ItemStack[] materials, Capability capability) {
        if (targetStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) targetStack.getItem();
            return item.getRepairRequiredCapabilityLevel(targetStack, capability);
        }
        return 0;
    }

    @Override
    public int getExperienceCost(ItemStack targetStack, ItemStack[] materials) {
        if (targetStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) targetStack.getItem();
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
