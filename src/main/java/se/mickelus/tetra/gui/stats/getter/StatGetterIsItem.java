package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class StatGetterIsItem implements IStatGetter {
    boolean inverted;
    List<Item> items;

    public StatGetterIsItem(List<Item> items, boolean inverted) {
        this.inverted = inverted;
        this.items = items;
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        return inverted != items.stream()
                .anyMatch(item -> currentStack.is(item) || previewStack.is(item));
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return shouldShow(player, itemStack, itemStack) ? 1 : 0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return shouldShow(player, itemStack, itemStack) ? 1 : 0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return shouldShow(player, itemStack, itemStack) ? 1 : 0;
    }
}
