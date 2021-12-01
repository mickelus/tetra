package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
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
    public double getValue(Player player, ItemStack itemStack) {
        return base + CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getEnchantmentLevelFromImprovements(itemStack, enchantment) * multiplier)
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return base + CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getEnchantmentLevelFromImprovements(itemStack, slot, enchantment) * multiplier)
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return base + CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getEnchantmentLevelFromImprovements(itemStack, slot, improvement, enchantment) * multiplier)
                .orElse(0d);
    }
}
