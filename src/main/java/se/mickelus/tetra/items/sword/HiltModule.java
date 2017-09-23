package se.mickelus.tetra.items.sword;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.ModuleData;
import se.mickelus.tetra.module.WeaponModuleData;

public class HiltModule extends ItemModuleMajor<ModuleData> {

    public static final String key = "basic_hilt";
	public static final String materialKey = "basic_hilt_material";

    public static HiltModule instance;

    public HiltModule(String slotKey) {
	    super(slotKey, key, materialKey);

	    data = new WeaponModuleData[] {
			    new WeaponModuleData("basic_hilt/hilt_stick", "minecraft:stick", 10, 1, 0),
			    new WeaponModuleData("basic_hilt/oak", "minecraft:planks", 24, 3, 0),
			    new WeaponModuleData("basic_hilt/iron", "minecraft:iron_ingot", 32, 4, 0),
			    new WeaponModuleData("basic_hilt/bone", "minecraft:bone", 32, 4, 0),
			    new WeaponModuleData("basic_hilt/blaze_rod", "minecraft:blaze_rod", 80, 6, 0),
			    new WeaponModuleData("basic_hilt/end_rod", "minecraft:end_rod", 106, 8, 0)
	    };

        instance = this;
        ItemUpgradeRegistry.instance.registerModule(key, this);
    }
    @Override
    public ItemStack[] removeModule(ItemStack targetStack, ItemStack[] tools) {
        return new ItemStack[0];
    }
}
