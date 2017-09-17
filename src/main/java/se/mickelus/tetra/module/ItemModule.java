package se.mickelus.tetra.module;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public abstract class ItemModule {
    public abstract String getName(ItemStack itemStack);
    public abstract void addModule(ItemStack targetStack, ItemStack[] materials);
    public abstract ItemStack[] removeModule(ItemStack targetStack, ItemStack[] tools);

    public abstract int getIntegrity(ItemStack itemStack);
    public abstract int getDurability(ItemStack itemStack);

    public double getDamageModifier(ItemStack itemStack) { return 0; }
    public double getDamageMultiplierModifier(ItemStack itemStack) { return 1; }

	public ResourceLocation[] getTextures(ItemStack itemStack) { return new ResourceLocation[0]; };
	public ResourceLocation[] getAllTextures() { return new ResourceLocation[0]; };

}
