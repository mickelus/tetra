package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StatGetterSpread extends StatGetterEffectEfficiency {

    public StatGetterSpread(ItemEffect effect) {
        super(effect, 1);
    }

    protected double wrapEfficiency(double eff) {
        return Math.max(0, 100 - eff);
    }

    private double offsetToAngle(double eff) {
        return Math.atan(Math.sqrt(2 * Math.pow(wrapEfficiency(eff) * 0.0172275d, 2))) * 180 / Math.PI;
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        return super.getValue(player, currentStack) > 0 || super.getValue(player, previewStack) > 0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return offsetToAngle(super.getValue(player, itemStack));
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return offsetToAngle(super.getValue(player, itemStack, slot));
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return offsetToAngle(super.getValue(player, itemStack, slot, improvement));
    }
}
