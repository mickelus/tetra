package se.mickelus.tetra.items.sword;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.UpgradeSchema;

public class BasicHiltSchema implements UpgradeSchema {

    private String key = "basic_hilt";

    public BasicHiltSchema() {
        ItemUpgradeRegistry.instance.registerSchema(this);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return "Replace hilt";
    }

    @Override
    public String getDescription() {
        return "Replace the current hilt with the given hilt, or a hilt crafted from raw materials.";
    }

    @Override
    public int getNumMaterialSlots() {

        return 1;
    }

    @Override
    public String getSlotName(final int index) {

        return "Hilt material";
    }

    @Override
    public boolean slotAcceptsStack(final int index, final ItemStack itemStack) {

        return true;
    }

    @Override
    public boolean canUpgrade(ItemStack itemStack) {
        return itemStack.getItem() instanceof ItemSwordModular;
    }

    @Override
    public ItemStack applyUpgrade(final ItemStack itemStack, final ItemStack[] materials) {
        ItemStack upgradedStack = itemStack.copy();

        HiltModule.instance.addModule(upgradedStack, materials);

        return upgradedStack;
    }
}
