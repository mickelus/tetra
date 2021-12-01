package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.util.CastOptional;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class StatGetterMagicCapacity implements IStatGetter {

    public StatGetterMagicCapacity() { }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getMajorModules(itemStack))
                .map(Arrays::stream)
                .orElse(Stream.empty())
                .filter(Objects::nonNull)
                .mapToInt(module -> module.getMagicCapacity(itemStack))
                .sum();
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot).getMagicCapacity(itemStack))
                .orElse(0);
    }

    public boolean hasGain(ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getMajorModules(itemStack))
                .map(Arrays::stream)
                .orElse(Stream.empty())
                .filter(Objects::nonNull)
                .mapToInt(module -> module.getMagicCapacityGain(itemStack))
                .anyMatch(gain -> gain > 0);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return 0;
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        return hasGain(currentStack) || hasGain(previewStack);
    }
}
