package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.effect.ChargedAbilityEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;

// todo: doesn't really work for bars since improvement & slot getters only reference the base item
public class StatGetterCooldown implements IStatGetter {

    private double offset = 0;
    private double multiplier = 1;


    public StatGetterCooldown(double offset, double multiplier) {
        this.offset = offset;
        this.multiplier = multiplier;
    }

    public StatGetterCooldown() { }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), ItemModularHandheld.class)
                .map(item -> item.getCooldownBase(itemStack))
                .orElse(0d) * multiplier + offset;
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot) {
        return getValue(player, itemStack);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return getValue(player, itemStack);
    }
}
