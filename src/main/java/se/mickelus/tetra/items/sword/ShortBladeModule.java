package se.mickelus.tetra.items.sword;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.WeaponModuleData;

public class ShortBladeModule extends ItemModuleMajor<WeaponModuleData> {

    public static final String key = "short_blade";
    public static final String materialKey = "short_blade_material";

    public static ShortBladeModule instance;

    public ShortBladeModule(String slotKey) {
        super(slotKey, key, materialKey);

        data = new WeaponModuleData[] {
            new WeaponModuleData("short_blade/oak", "minecraft:planks", 30, 1, 1, 2.4f, 0x866526, 32, 0),
            new WeaponModuleData("short_blade/iron", "minecraft:iron_ingot", 120, 1, 2, 1, 0xd8d8d8, 32, 0),
            new WeaponModuleData("short_blade/gold", "minecraft:gold_ingot", 10, 1, 2, 1.5f, 0xeaee57, 32, 0),
        };

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
