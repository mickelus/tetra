package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.ReachingEffect;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StatGetterReaching extends StatGetterEffectLevel {
    public StatGetterReaching() {
        super(ItemEffect.reaching, 1);
    }

    @Override
    public boolean shouldShow(Player player, ItemStack currentStack, ItemStack previewStack) {
        return super.getValue(player, currentStack) > 0 || super.getValue(player, previewStack) > 0;
    }

    @Override
    public double getValue(Player player, ItemStack itemStack) {
        return 100 * ReachingEffect.getOffset((int) super.getValue(player, itemStack), 3);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot) {
        return 100 * ReachingEffect.getOffset((int) super.getValue(player, itemStack, slot), 3);
    }

    @Override
    public double getValue(Player player, ItemStack itemStack, String slot, String improvement) {
        return 100 * ReachingEffect.getOffset((int) super.getValue(player, itemStack, slot, improvement), 3);
    }
}
