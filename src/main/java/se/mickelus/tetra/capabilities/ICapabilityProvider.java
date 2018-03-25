package se.mickelus.tetra.capabilities;

import net.minecraft.item.ItemStack;

import java.util.Collection;

public interface ICapabilityProvider {
    public int getCapabilityLevel(ItemStack itemStack, Capability capability);
    public float getCapabilityEfficiency(ItemStack itemStack, Capability capability);
    public Collection<Capability> getCapabilities(ItemStack itemStack);
}
