package se.mickelus.tetra.module;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;

import java.util.Collection;

public interface UpgradeSchema {

    public String getKey();
    public String getName();
    public String getDescription();
    public int getNumMaterialSlots();
    public String getSlotName(final ItemStack itemStack, int index);
    public int getRequiredQuantity(final ItemStack itemStack, int index, ItemStack materialStack);

    /**
     * Returns true if the provided materialStack is accepted in the slot at the given index, for the given upgrade
     * target.
     * A slot may accept a material without a craft actually being possible using the material.
     * @param itemStack the itemstack that is to be upgraded
     * @param index the index of the slot
     * @param materialStack the upgrade material
     * @return
     */
    public boolean slotAcceptsMaterial(ItemStack itemStack, int index, ItemStack materialStack);

    /**
     * Returns true if all material slot contain valid material of enough quantity. Craft is possible using the provided
     * materials.
     * @param itemStack the itemstack that is to be upgraded
     * @param materials the materials to be used for the upgrade
     * @return
     */
    public boolean isMaterialsValid(ItemStack itemStack, ItemStack[] materials);

    public boolean canUpgrade(ItemStack itemStack);
    public boolean canApplyUpgrade(EntityPlayer player, ItemStack itemStack, ItemStack[] materials);
    public boolean isIntegrityViolation(ItemStack itemStack, ItemStack[] materials);
    public ItemStack applyUpgrade(ItemStack itemStack, ItemStack[] materials, boolean consumeMaterials);

    public boolean checkCapabilities(EntityPlayer player, final ItemStack targetStack, final ItemStack[] materials);
    public Collection<Capability> getRequiredCapabilities(final ItemStack targetStack, final ItemStack[] materials);
    public int getRequiredCapabilityLevel(final ItemStack targetStack, final ItemStack[] materials, Capability capability);
}
