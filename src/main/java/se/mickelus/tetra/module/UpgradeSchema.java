package se.mickelus.tetra.module;

import net.minecraft.item.ItemStack;

public interface UpgradeSchema {

    public String getKey();
    public String getName();
    public String getDescription();
    public int getNumMaterialSlots();
    public String getSlotName(int index);
	public boolean slotAcceptsMaterial(ItemStack itemStack, int index, ItemStack materialStack);

    public boolean canUpgrade(ItemStack itemStack);
    public boolean canApplyUpgrade(ItemStack itemStack, ItemStack[] materials);
    public ItemStack applyUpgrade(ItemStack itemStack, ItemStack[] materials, boolean consumeMaterials);
}
