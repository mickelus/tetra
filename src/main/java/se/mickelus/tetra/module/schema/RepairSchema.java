package se.mickelus.tetra.module.schema;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

import java.util.Collection;
import java.util.Collections;

public class RepairSchema implements UpgradeSchema {
    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";

    private String key = "repair_schema";

    private ItemModular item;

    private GlyphData glyph = new GlyphData("textures/gui/workbench.png", 0, 52);

    public RepairSchema(ItemModular item) {
        this.item = item;
        ItemUpgradeRegistry.instance.registerSchema(this);
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
    public String getDescription() {
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
        // todo: return random between 1 and materialCount based on seed and yield % of module durability based on quantity?
        return 1;
    }

    @Override
    public boolean acceptsMaterial(final ItemStack itemStack, final int index, final ItemStack materialStack) {
        if (index == 0 && itemStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) itemStack.getItem();
            return materialStack.isItemEqual(item.getRepairMaterial(itemStack));
        }
        return false;
    }

    @Override
    public boolean canUpgrade(ItemStack itemStack) {
        return item.getClass().isInstance(itemStack.getItem());
    }

    @Override
    public boolean isApplicableForSlot(String slot) {
        return slot == null;
    }

    @Override
    public boolean isHoning() {
        return false;
    }

    @Override
    public boolean canApplyUpgrade(EntityPlayer player, ItemStack itemStack, ItemStack[] materials, String slot) {
        return acceptsMaterial(itemStack, 0, materials[0]);
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, ItemStack[] materials) {
        return acceptsMaterial(itemStack, 0, materials[0]);
    }

    @Override
    public boolean isIntegrityViolation(EntityPlayer player, ItemStack itemStack, final ItemStack[] materials, String slot) {
        return false;
    }

    @Override
    public ItemStack applyUpgrade(final ItemStack itemStack, final ItemStack[] materials, boolean consumeMaterials, String slot, EntityPlayer player) {
        ItemStack upgradedStack = itemStack.copy();
        ItemModular item = (ItemModular) upgradedStack.getItem();

        item.repair(upgradedStack);

        if (consumeMaterials) {
            materials[0].shrink(1);
        }

        return upgradedStack;
    }

    @Override
    public boolean checkCapabilities(EntityPlayer player,final ItemStack targetStack,  final ItemStack[] materials) {
        return getRequiredCapabilities(targetStack, materials).stream()
                .allMatch(capability -> CapabilityHelper.getCapabilityLevel(player, capability) >= getRequiredCapabilityLevel(targetStack, materials, capability));
    }

    @Override
    public Collection<Capability> getRequiredCapabilities(final ItemStack targetStack, final ItemStack[] materials) {
        // todo: use same capability as target module
        if (targetStack.getItem() instanceof ItemModular) {
            ItemModular item = (ItemModular) targetStack.getItem();
            return item.getRepairRequiredCapabilities(targetStack);
        }
        return Collections.EMPTY_LIST;
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
    public SchemaType getType() {
        return SchemaType.other;
    }

    @Override
    public GlyphData getGlyph() {
        return glyph;
    }
}
