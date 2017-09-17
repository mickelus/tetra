package se.mickelus.tetra.items.sword;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

public class HiltModule extends ItemModuleMajor {

    public static final String key = "basic_hilt";

    public static HiltModule instance;

    public HiltModule() {
        instance = this;
        ItemUpgradeRegistry.instance.registerModule(key, this);
    }

    @Override
    public String getName(ItemStack stack) {
        return "Wooden hilt";
    }

    @Override
    public void addModule(ItemStack targetStack, ItemStack[] materials) {
        NBTTagCompound tag = targetStack.getTagCompound();

        tag.setString(ItemSwordModular.hiltKey, key);
    }

    @Override
    public ItemStack[] removeModule(ItemStack targetStack, ItemStack[] tools) {
        return new ItemStack[0];
    }

	@Override
	public ResourceLocation[] getTextures(final ItemStack itemStack) {
		return new ResourceLocation[] {new ResourceLocation(TetraMod.MOD_ID, "items/sword_modular/hilt_stick")};
	}

	@Override
	public ResourceLocation[] getAllTextures() {
		return new ResourceLocation[] {new ResourceLocation(TetraMod.MOD_ID, "items/sword_modular/hilt_stick")};
	}
}
