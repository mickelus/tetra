package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.util.CastOptional;

public class StatGetterEnchantmentLevel implements IStatGetter {

    private Enchantment enchantment;

    private double multiplier;
    private double base;

    public StatGetterEnchantmentLevel(Enchantment enchantment, double multiplier) {
        this(enchantment, multiplier, 0);
    }

    public StatGetterEnchantmentLevel(Enchantment enchantment, double multiplier, double base) {
        this.enchantment = enchantment;
        this.multiplier = multiplier;
        this.base = base;
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack) {
        return base + CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getEnchantmentLevelFromImprovements(itemStack, enchantment) * multiplier)
                .orElse(0d);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot) {
        return base + CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getEnchantmentLevelFromImprovements(itemStack, slot, enchantment) * multiplier)
                .orElse(0d);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return base + CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getEnchantmentLevelFromImprovements(itemStack, slot, improvement, enchantment) * multiplier)
                .orElse(0d);
    }
}
