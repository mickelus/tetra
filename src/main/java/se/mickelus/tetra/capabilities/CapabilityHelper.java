package se.mickelus.tetra.capabilities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CapabilityHelper {

    public static int getCapabilityLevel(EntityPlayer player, Capability capability) {
        return Stream.concat(player.inventory.mainInventory.stream(), player.inventory.offHandInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(itemStack -> {
                    ItemStack replacement = ItemUpgradeRegistry.instance.getReplacement(itemStack);
                    if (!replacement.isEmpty()) {
                        return replacement;
                    }
                    return itemStack;
                })
                .filter(itemStack -> itemStack.getItem() instanceof ICapabilityProvider)
                .map(itemStack -> ((ICapabilityProvider) itemStack.getItem()).getCapabilityLevel(itemStack, capability))
                .max(Integer::compare)
                .orElse(0);
    }

    public static Collection<Capability> getCapabilities(EntityPlayer player) {
        return Stream.concat(player.inventory.mainInventory.stream(), player.inventory.offHandInventory.stream())
                .filter(itemStack -> !itemStack.isEmpty())
                .map(itemStack -> {
                    ItemStack replacement = ItemUpgradeRegistry.instance.getReplacement(itemStack);
                    if (!replacement.isEmpty()) {
                        return replacement;
                    }
                    return itemStack;
                })
                .filter(itemStack -> itemStack.getItem() instanceof ICapabilityProvider)
                .flatMap(itemStack -> ((ICapabilityProvider) itemStack.getItem()).getCapabilities(itemStack).stream())
                .collect(Collectors.toSet());
    }
}
