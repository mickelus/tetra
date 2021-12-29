package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.util.CastOptional;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class StatGetterUnbreaking implements IStatGetter {
    private final IStatGetter levelGetter;

    public StatGetterUnbreaking(IStatGetter levelGetter) {
        this.levelGetter = levelGetter;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return 100 - 100d / (levelGetter.getValue(player, itemStack) + 1);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        double levelItem = levelGetter.getValue(player, itemStack);

        return Optional.of(levelGetter.getValue(player, itemStack, slot))
                .map(level -> 100d / (levelItem - level + 1) - 100d / (levelItem + 1))
                .orElse(0d);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        double levelItem = levelGetter.getValue(player, itemStack);

        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getImprovement(itemStack, improvement))
                .map(data -> data.effects.getLevel(ItemEffect.unbreaking))
                .map(level -> 100d / (levelItem - level + 1) - 100d / (levelItem + 1))
                .orElse(0d);
    }
}
