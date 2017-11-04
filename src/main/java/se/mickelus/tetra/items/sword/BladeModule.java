package se.mickelus.tetra.items.sword;

import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.WeaponModuleData;

public class BladeModule extends ItemModuleMajor<WeaponModuleData> {

    public static final String key = "basic_blade";
    public static final String materialKey = "basic_blade_material";

    public static BladeModule instance;

    public BladeModule(String slotKey) {
        super(slotKey, key, materialKey);

        data = new WeaponModuleData[] {
            new WeaponModuleData("basic_blade/oak", "minecraft:planks", 100, 0, 1, 0x866526, 0, 0),
            new WeaponModuleData("basic_blade/cobblestone", "minecraft:cobblestone", 150, -1, 2, 0x9a9a9a, 0, 0),
            new WeaponModuleData("basic_blade/stone", "minecraft:stone", 300, -1, 3, 0x9a9a9a, 0, 0),
            new WeaponModuleData("basic_blade/iron", "minecraft:iron_ingot", 300, 0, 3, 0xd8d8d8, 0, 0),
            new WeaponModuleData("basic_blade/gold", "minecraft:gold_ingot", 30, 0, 4, 0xeaee57, 0, 0),
            new WeaponModuleData("basic_blade/diamond", "minecraft:diamond", 540, -1, 5, 0x33ebcb, 0, 0),
            new WeaponModuleData("basic_blade/obsidian", "minecraft:obsidian", 720, -3, 5, 0x3c3056, 0, 0)
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
}
