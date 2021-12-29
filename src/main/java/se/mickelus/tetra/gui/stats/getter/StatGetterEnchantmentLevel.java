package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.core.Registry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StatGetterEnchantmentLevel implements IStatGetter {

    private final Enchantment enchantment;
    private final String enchantmentKey;

    private final double multiplier;
    private final double base;

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
    public double getValue(Player player, ItemStack itemStack) {
        return base + CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemStack) * multiplier)
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return base + CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getEnchantments(itemStack))
                .map(enchantments -> enchantments.get(enchantment))
                .map(level -> level * multiplier)
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        if (improvement.startsWith("enchantment:") && enchantmentKey.equals(improvement.substring(12))) {
            return base + getValue(player, itemStack);
        }

        return base;
    }
}
