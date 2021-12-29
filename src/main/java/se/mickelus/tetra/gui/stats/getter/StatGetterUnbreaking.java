package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.util.CastOptional;

import java.util.Optional;

public class StatGetterUnbreaking implements IStatGetter {

    private final IStatGetter levelGetter;

    public StatGetterUnbreaking(IStatGetter levelGetter) {
        this.levelGetter = levelGetter;
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack) {
        return Optional.of(levelGetter.getValue(player, itemStack))
                .map(level -> 100 - 100d / (level + 1))
                .orElse(0d);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot) {
        double levelItem = levelGetter.getValue(player, itemStack);

        return Optional.of(levelGetter.getValue(player, itemStack, slot))
                .map(level -> 100d / (levelItem - level + 1) - 100d / (levelItem + 1))
                .orElse(0d);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        double levelItem = levelGetter.getValue(player, itemStack);

        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getImprovement(itemStack, improvement))
                .map(data -> data.effects.getLevel(ItemEffect.unbreaking))
                .map(level -> 100d / (levelItem - level + 1) - 100d / (levelItem + 1))
                .orElse(0d);
    }
}
