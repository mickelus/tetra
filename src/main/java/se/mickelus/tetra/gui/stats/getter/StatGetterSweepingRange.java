package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StatGetterSweepingRange extends StatGetterEffectEfficiency {

    IStatGetter levelGetter;

    public StatGetterSweepingRange() {
        super(ItemEffect.sweeping, 1);
        levelGetter = new StatGetterEffectLevel(ItemEffect.sweeping, 1);
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        return !levelGetter.shouldShow(player, currentStack, previewStack) && super.shouldShow(player, currentStack, previewStack);
    }
}
