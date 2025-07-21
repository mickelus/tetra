package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StatGetterClamp implements IStatGetter {
    private final IStatGetter statGetter;

    private Double min;
    private Double max;

    public StatGetterClamp(IStatGetter statGetter, Double min, Double max) {
        this.statGetter = statGetter;
        this.min = min;
        this.max = max;
    }

    private double clamp(double value) {
        if (min != null && value < min) {
            return min;
        }
        if (max != null && value > max) {
            return max;
        }
        return value;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return clamp(statGetter.getValue(player, itemStack));
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return clamp(statGetter.getValue(player, itemStack, slot));
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return clamp(statGetter.getValue(player, itemStack, slot, improvement));
    }
}
