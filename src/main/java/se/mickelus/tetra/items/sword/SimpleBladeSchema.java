package se.mickelus.tetra.items.sword;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.UpgradeSchema;

public class SimpleBladeSchema implements UpgradeSchema {

    private String key = "simple_blade";

    public SimpleBladeSchema() {
        ItemUpgradeRegistry.instance.registerSchema(this);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return "Replace blade";
    }

    @Override
    public String getDescription() {
        return "Replace the current sword blade with the given blade, or reforge it using raw materials.";
    }

    @Override
    public int getNumMaterialSlots() {

        return 1;
    }

    @Override
    public String getSlotName(final int index) {

        return "Blade material";
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

        BladeModule.instance.addModule(upgradedStack, materials);

        return upgradedStack;
    }
}
