package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StatGetterEffectEfficiency implements IStatGetter {

    private final ItemEffect effect;

    private final double multiplier;
    private final double base;

    public StatGetterEffectEfficiency(ItemEffect effect, double multiplier) {
        this(effect, multiplier, 0);
    }

    public StatGetterEffectEfficiency(ItemEffect effect, double multiplier, double base) {
        this.effect = effect;
        this.multiplier = multiplier;
        this.base = base;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return base + CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getEffectEfficiency(itemStack, effect) * multiplier)
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getEffectEfficiency(itemStack, effect) * multiplier)
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return base + CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getImprovement(itemStack, improvement))
                .map(improvementData -> improvementData.effects)
                .map(effects -> effects.getEfficiency(effect) * multiplier)
                .orElse(0d);
    }
}
