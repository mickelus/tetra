package se.mickelus.tetra.items.sword;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

public class BladeModule extends ItemModuleMajor {

    public static final String key = "basic_blade";
    public static final String materialKey = "basic_blade_material";

    public static BladeModule instance;

    public BladeModule() {
        instance = this;
        ItemUpgradeRegistry.instance.registerModule(key, this);
    }

    @Override
    public String getName(ItemStack itemStack) {
	    NBTTagCompound tag = itemStack.getTagCompound();
	    String materialName = tag.getString(materialKey);

	    switch (materialName) {
		    case "minecraft:planks":
		    	return "Sharpened Plank";
		    case "minecraft:cobblestone":
			    return "Sharpened Stone";
		    case "minecraft:iron_ingot":
			    return "Iron Blade";
		    case "minecraft:gold_ingot":
			    return "Golden Blade";
		    case "minecraft:diamond":
			    return "Diamond Blade";
	    }
	    return "Wooden Blade";
    }

    @Override
    public void addModule(ItemStack targetStack, ItemStack[] materials) {
        NBTTagCompound tag = targetStack.getTagCompound();
	    ResourceLocation resourcelocation = Item.REGISTRY.getNameForObject(materials[0].getItem());

        tag.setString(ItemSwordModular.bladeKey, key);
        tag.setString(materialKey, resourcelocation.toString());
    }

    @Override
    public ItemStack[] removeModule(ItemStack targetStack, ItemStack[] tools) {
        return new ItemStack[0];
    }
}
