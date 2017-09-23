package se.mickelus.tetra.module;

import java.util.Arrays;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.NBTHelper;

public abstract class ItemModule<T extends ModuleData> {

	protected T[] data;

	protected String slotKey;
	protected String moduleKey;
	protected String dataKey;

	public ItemModule(String slotKey, String moduleKey, String dataKey) {
		this.slotKey = slotKey;
		this.moduleKey = moduleKey;
		this.dataKey = dataKey;
	}

	public void addModule(ItemStack targetStack, ItemStack[] materials) {
		NBTTagCompound tag = NBTHelper.getTag(targetStack);

		tag.setString(slotKey, moduleKey);
		tag.setString(dataKey, getDataByMaterial(materials[0]).key);
	}

	private T getDataByMaterial(ItemStack itemStack) {
		String material = Item.REGISTRY.getNameForObject(itemStack.getItem()).toString();

		return Arrays.stream(data)
				.filter(moduleData -> moduleData.material.equals(material))
				.findAny().orElse(getDefaultData());
	}

	public ItemStack[] removeModule(ItemStack targetStack, ItemStack[] tools) {
		NBTTagCompound tag = NBTHelper.getTag(targetStack);

		tag.removeTag(slotKey);
		tag.removeTag(dataKey);

		return new ItemStack[0];
	}

	public T getData(ItemStack itemStack) {
		NBTTagCompound tag = NBTHelper.getTag(itemStack);
		String dataName = tag.getString(this.dataKey);

		return Arrays.stream(data)
				.filter(moduleData -> moduleData.key.equals(dataName))
				.findAny().orElse(getDefaultData());
	}

	protected T getDefaultData() {
		return data[0];
	}

    public String getName(ItemStack itemStack) {
	    return I18n.format(getData(itemStack).key);
    }

    public int getIntegrity(ItemStack itemStack) {
    	return getData(itemStack).integrity;
    }
    public int getDurability(ItemStack itemStack) {
    	return getData(itemStack).durability;
    }

    public double getDamageModifier(ItemStack itemStack) { return 0; }
    public double getDamageMultiplierModifier(ItemStack itemStack) { return 1; }

	public ResourceLocation[] getTextures(ItemStack itemStack) {
        return new ResourceLocation[] { getData(itemStack).textureLocation };
	}

	public ResourceLocation[] getAllTextures() {
		return Arrays.stream(data)
				.map(moduleData -> moduleData.textureLocation)
				.toArray(ResourceLocation[]::new);
	}
}
