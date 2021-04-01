package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.effect.ChargedAbilityEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;

// todo: doesn't really work for bars since improvement & slot getters only reference the base item
public class StatGetterAbilityCooldown implements IStatGetter {
    private ChargedAbilityEffect ability;

    public StatGetterAbilityCooldown(ChargedAbilityEffect ability) {
        this.ability = ability;
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), ItemModularHandheld.class)
                .map(item -> ability.getCooldown(item, itemStack))
                .map(ticks -> ticks / 20d)
                .orElse(0d);
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
