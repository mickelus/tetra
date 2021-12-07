package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.mutil.util.CastOptional;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class StatGetterAbilityDamage implements IStatGetter {
    private final double offset;
    private final double multiplier;

    public StatGetterAbilityDamage(double offset, double multiplier) {
        this.offset = offset;
        this.multiplier = multiplier;
    }

    public StatGetterAbilityDamage() {
        this(0, 1);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), ItemModularHandheld.class)
                .map(item -> item.getAbilityBaseDamage(itemStack))
                .orElse(0d) * multiplier + offset;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return getValue(player, itemStack);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return getValue(player, itemStack);
    }
}
