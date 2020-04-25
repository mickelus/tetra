package se.mickelus.tetra.capabilities;

import net.minecraft.item.ItemStack;

import java.util.Set;

public interface ICapabilityProvider {
    public int getCapabilityLevel(ItemStack itemStack, Capability capability);
    public float getCapabilityEfficiency(ItemStack itemStack, Capability capability);
    public Set<Capability> getCapabilities(ItemStack itemStack);
}
