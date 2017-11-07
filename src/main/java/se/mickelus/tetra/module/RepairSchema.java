package se.mickelus.tetra.module;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.ItemModular;

public class RepairSchema implements UpgradeSchema {
    private static final String nameSuffix = ".name";
    private static final String descriptionSuffix = ".description";
    private static final String slotSuffix = ".slot1";

    private String key = "repair_schema";

    private ItemModular item;

    public RepairSchema(ItemModular item) {
        this.item = item;
        ItemUpgradeRegistry.instance.registerSchema(this);
    }

    @Override
    public String getKey() {
        return key;
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
    public boolean slotAcceptsMaterial(final ItemStack itemStack, final int index, final ItemStack materialStack) {
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
    public boolean canApplyUpgrade(ItemStack itemStack, ItemStack[] materials) {
        return slotAcceptsMaterial(itemStack, 0, materials[0]);
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, ItemStack[] materials) {
        return slotAcceptsMaterial(itemStack, 0, materials[0]);
    }

    @Override
    public boolean isIntegrityViolation(ItemStack itemStack, final ItemStack[] materials) {
        return false;
    }

    @Override
    public ItemStack applyUpgrade(final ItemStack itemStack, final ItemStack[] materials, boolean consumeMaterials) {
        ItemStack upgradedStack = itemStack.copy();
        ItemModular item = (ItemModular) upgradedStack.getItem();

        item.repair(upgradedStack);

        if (consumeMaterials) {
            materials[0].shrink(1);
        }

        return upgradedStack;
    }
}
