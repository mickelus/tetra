package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StatGetterEffectLevel implements IStatGetter {

    protected final ItemEffect effect;

    protected final double multiplier;
    protected final double base;

    protected final boolean showNegative;


    public StatGetterEffectLevel(ItemEffect effect, double multiplier, double base, boolean showNegative) {
        this.effect = effect;
        this.multiplier = multiplier;
        this.base = base;
        this.showNegative = showNegative;
    }


    public StatGetterEffectLevel(ItemEffect effect, double multiplier, double base) {
        this(effect, multiplier, base, false);
    }

    public StatGetterEffectLevel(ItemEffect effect, double multiplier) {
        this(effect, multiplier, 0);
    }

    public StatGetterEffectLevel(ItemEffect effect, boolean showNegative) {
        this(effect, 1, 0, showNegative);
    }

    public StatGetterEffectLevel(ItemEffect effect) {
        this(effect, false);
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        double baseValue = getValue(player, ItemStack.EMPTY);
        return showNegative
                ? getValue(player, currentStack) != baseValue || getValue(player, previewStack) != baseValue
                : getValue(player, currentStack) > baseValue || getValue(player, previewStack) > baseValue;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return base + CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getEffectLevel(itemStack, effect) * multiplier)
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getEffectLevel(itemStack, effect) * multiplier)
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return base + CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getImprovement(itemStack, improvement))
                .map(improvementData -> improvementData.effects)
                .map(effects -> effects.getLevel(effect) * multiplier)
                .orElse(0d);
    }
}
