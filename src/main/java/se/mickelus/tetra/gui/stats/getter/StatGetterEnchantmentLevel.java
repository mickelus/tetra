package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.aspect.TetraEnchantmentHelper;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StatGetterEnchantmentLevel implements IStatGetter {

    private final ResourceKey<Enchantment> enchantment;
    private final String enchantmentKey;

    private final double multiplier;
    private final double base;

    public StatGetterEnchantmentLevel(ResourceKey<Enchantment> enchantment, double multiplier, double base) {
        this.enchantment = enchantment;
        this.enchantmentKey = enchantment.location().toString();
        this.multiplier = multiplier;
        this.base = base;
    }

    public StatGetterEnchantmentLevel(Enchantment enchantment, double multiplier, double base) {
        this(ResourceKey.create(Registries.ENCHANTMENT,
                TetraEnchantmentHelper.getEnchantmentKey(enchantment)
                        .orElseThrow(() -> new IllegalStateException("Unregistered enchantment: " + enchantment))),
                multiplier, base);
    }

    public StatGetterEnchantmentLevel(ResourceKey<Enchantment> enchantment, double multiplier) {
        this(enchantment, multiplier, 0);
    }

    public StatGetterEnchantmentLevel(Enchantment enchantment, double multiplier) {
        this(enchantment, multiplier, 0);
    }

    public StatGetterEnchantmentLevel(ResourceKey<Enchantment> enchantment) {
        this(enchantment, 1, 0);
    }

    public StatGetterEnchantmentLevel(Enchantment enchantment) {
        this(enchantment, 1, 0);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> EffectHelper.getEnchantmentLevel(enchantment, itemStack))
                .filter(level -> level > 0)
                .map(level -> level.doubleValue() * multiplier + base)
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getEnchantmentsPrimitive(itemStack))
                .map(enchantments -> enchantments.get(enchantmentKey))
                .filter(level -> level > 0)
                .map(level -> level.doubleValue() * multiplier + base)
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        if (improvement.startsWith("enchantment:") && enchantmentKey.equals(improvement.substring(12))) {
            return getValue(player, itemStack);
        }

        return 0;
    }
}
