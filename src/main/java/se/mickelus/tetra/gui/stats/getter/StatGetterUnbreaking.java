package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class StatGetterUnbreaking implements IStatGetter {

    public StatGetterUnbreaking() { }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getEffectLevel(itemStack, ItemEffect.unbreaking))
                .map(level -> 100 - 100d / (level + 1))
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        int levelItem = CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getEffectLevel(itemStack, ItemEffect.unbreaking))
                .orElse(0);

        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModule.class))
                .map(module -> module.getEffectLevel(itemStack, ItemEffect.unbreaking))
                .map(level -> 100d / (levelItem - level + 1) - 100d / (levelItem + 1))
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        int levelItem = CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getEffectLevel(itemStack, ItemEffect.unbreaking))
                .orElse(0);

        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getImprovement(itemStack, improvement))
                .map(data -> data.effects.getLevel(ItemEffect.unbreaking))
                .map(level -> 100d / (levelItem - level + 1) - 100d / (levelItem + 1))
                .orElse(0d);
    }
}
