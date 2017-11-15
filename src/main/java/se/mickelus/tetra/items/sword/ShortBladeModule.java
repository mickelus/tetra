package se.mickelus.tetra.items.sword;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.DataHandler;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.WeaponModuleData;

public class ShortBladeModule extends ItemModuleMajor<WeaponModuleData> {

    public static final String key = "short_blade";
    public static final String materialKey = "short_blade_material";

    public static ShortBladeModule instance;

    public ShortBladeModule(String slotKey) {
        super(slotKey, key, materialKey);

        data = DataHandler.instance.getModuleData(key, WeaponModuleData[].class);

        instance = this;
        ItemUpgradeRegistry.instance.registerModule(key, this);
    }

    @Override
    public ItemStack[] removeModule(ItemStack targetStack, ItemStack[] tools) {
        return new ItemStack[0];
    }

    @Override
    public double getDamageModifier(ItemStack itemStack) {
        return getData(itemStack).damage;
    }

    @Override
    public double getSpeedModifier(ItemStack itemStack) {
        return getData(itemStack).attackSpeed;
    }
}
