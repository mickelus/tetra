package se.mickelus.tetra.module;

import net.minecraft.item.ItemStack;

public interface UpgradeSchema {

    public String getName();
    public String getDescription();
    public int getNumMaterialSlots();
    public String getSlotName(int index);
	public boolean slotAcceptsStack(int index, ItemStack itemStack);
    public ItemStack applyUpgrade(ItemStack itemStack, ItemStack[] materials);
}
