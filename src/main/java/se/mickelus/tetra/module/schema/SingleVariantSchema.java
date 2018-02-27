package se.mickelus.tetra.module.schema;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.ItemModule;

public class SingleVariantSchema extends BasicSchema {
    private String currentModuleKey;
    private String targetModuleKey;
    public SingleVariantSchema(String key, ItemModule module, Item item, String currentModuleKey, String targetModuleKey) {
        super(key, module, item);

        this.currentModuleKey = currentModuleKey;
        this.targetModuleKey = targetModuleKey;
    }

    @Override
    public boolean canUpgrade(ItemStack itemStack) {
        return super.canUpgrade(itemStack) && currentModuleKey.equals(module.getData(itemStack).key);
    }

    @Override
    public boolean slotAcceptsMaterial(ItemStack itemStack, int index, ItemStack materialStack) {
        return targetModuleKey.equals(module.getDataByMaterial(materialStack).key);
    }

    @Override
    public boolean isMaterialsValid(ItemStack itemStack, ItemStack[] materials) {
        return slotAcceptsMaterial(itemStack, 0, materials[0]);
    }
}
