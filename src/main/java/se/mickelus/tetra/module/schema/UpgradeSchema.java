package se.mickelus.tetra.module.schema;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.module.data.GlyphData;

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
    public boolean acceptsMaterial(ItemStack itemStack, int index, ItemStack materialStack);

    /**
     * Returns true if all material slot contain valid material of enough quantity. Craft is possible using the provided
     * materials.
     * @param itemStack the itemstack that is to be upgraded
     * @param materials the materials to be used for the upgrade
     * @return
     */
    public boolean isMaterialsValid(ItemStack itemStack, ItemStack[] materials);

    /**
     * Returns true if this schema can potentially be applied to the given itemstack.
     * @param itemStack An itemstack that is to be upgraded
     * @return
     */
    public boolean canUpgrade(ItemStack itemStack);

    /**
     * Returns true if this schema can be applied to the given slot of a modular item.
     * @param slot The slot to check if this schema is applicable for
     * @return
     */
    public boolean isApplicableForSlot(String slot);

    /**
     * Returns true if this is a honing schema and should have it's application regulated by honing rules.
     * @return
     */
    public boolean isHoning();

    /**
     * Returns true if this schema can actually be applied using the given set of materials and to the given slot.
     * @param player The player attempting to perform this
     * @param itemStack The itemstack that is to be upgraded
     * @param materials An array of itemstacks
     * @param slot The slot that the upgrade should be applied to
     * @return
     */
    public boolean canApplyUpgrade(EntityPlayer player, ItemStack itemStack, ItemStack[] materials, String slot);
    public boolean isIntegrityViolation(EntityPlayer player, ItemStack itemStack, ItemStack[] materials, String slot);
    public ItemStack applyUpgrade(ItemStack itemStack, ItemStack[] materials, boolean consumeMaterials, String slot, EntityPlayer player);

    public boolean checkCapabilities(EntityPlayer player, final ItemStack targetStack, final ItemStack[] materials);
    public Collection<Capability> getRequiredCapabilities(final ItemStack targetStack, final ItemStack[] materials);
    public int getRequiredCapabilityLevel(final ItemStack targetStack, final ItemStack[] materials, Capability capability);

    public SchemaType getType();
    public GlyphData getGlyph();
}
