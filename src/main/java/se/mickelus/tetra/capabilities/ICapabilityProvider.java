package se.mickelus.tetra.capabilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.Set;

public interface ICapabilityProvider {
    public int getCapabilityLevel(ItemStack itemStack, Capability capability);
    public float getCapabilityEfficiency(ItemStack itemStack, Capability capability);
    public Set<Capability> getCapabilities(ItemStack itemStack);

    /**
     * Apply special effects and possibly consume required resources after this item has been used to craft or upgrade
     * another item. This is called once for each capability used by the craft, which this item provide.
     * @param providerStack The providing stack, the itemstack for this item
     * @param targetStack The itemstack which is being upgraded/crafted/altered in some way
     * @param player The player performing the actions
     * @param capability The capability used
     * @param capabilityLevel The level of the used capability
     * @param consumeResources
     */
    public default ItemStack onCraftConsumeCapability(ItemStack providerStack, ItemStack targetStack, PlayerEntity player,
            Capability capability, int capabilityLevel, boolean consumeResources) {
        ItemStack result = targetStack.copy();

        return result;
    }

    /**
     * Apply special effects and possibly consume required resources after this item has been used to perform a
     * workbench action.
     * @param providerStack The providing stack, the itemstack for this item
     * @param targetStack The itemstack which the action is performed upon
     * @param player The player performing the action
     * @param capability The capability used
     * @param capabilityLevel The level of the used capability
     * @param consumeResources
     */
    public default ItemStack onActionConsumeCapability(ItemStack providerStack, ItemStack targetStack, PlayerEntity player,
            Capability capability, int capabilityLevel, boolean consumeResources) {
        ItemStack result = targetStack.copy();

        return result;
    }
}
