package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.util.CastOptional;

public class StatGetterEnchantmentLevel implements IStatGetter {

    private Enchantment enchantment;
    private String enchantmentKey;

    private double multiplier;
    private double base;

    public StatGetterEnchantmentLevel(Enchantment enchantment, double multiplier) {
        this(enchantment, multiplier, 0);
    }

    public StatGetterEnchantmentLevel(Enchantment enchantment, double multiplier, double base) {
        this.enchantment = enchantment;
        this.enchantmentKey = Registry.ENCHANTMENT.getKey(enchantment).toString();
        this.multiplier = multiplier;
        this.base = base;
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack) {
        return base + CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> EnchantmentHelper.getEnchantmentLevel(enchantment, itemStack) * multiplier)
                .orElse(0d);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot) {
        return base + CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getEnchantments(itemStack))
                .map(enchantments -> enchantments.get(enchantment))
                .map(level -> level * multiplier)
                .orElse(0d);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        if (improvement.startsWith("enchantment:") && enchantmentKey.equals(improvement.substring(12))) {
            return base + getValue(player, itemStack);
        }

        return base;
    }
}
