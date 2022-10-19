package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.effect.ReachingEffect;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TooltipGetterReaching implements ITooltipGetter {
    private final IStatGetter getter;
    private final IStatGetter reachGetter;
    private final IStatGetter rangeGetter;

    public TooltipGetterReaching() {
        this.getter = new StatGetterEffectLevel(ItemEffect.reaching, 1);
        reachGetter = new StatGetterAttribute(ForgeMod.REACH_DISTANCE.get(), false);
        rangeGetter = new StatGetterAttribute(ForgeMod.ATTACK_RANGE.get(), false);
    }


    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        return I18n.get("tetra.stats.reaching.tooltip",
                String.format("%.0f", 100 * ReachingEffect.getOffset((int) getter.getValue(player, itemStack), 3)), 3);
    }

    @Override
    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return true;
    }

    @Override
    public String getTooltipExtension(Player player, ItemStack itemStack) {
        int level = (int) getter.getValue(player, itemStack);
        double reach = reachGetter.getValue(player, itemStack);
        double range = rangeGetter.getValue(player, itemStack);
        return I18n.get("tetra.stats.reaching.tooltip_extended",
                String.format("%.0f", 100 * ReachingEffect.getOffset(level, reach)), String.format("%.1f", reach),
                String.format("%.0f", 100 * ReachingEffect.getOffset(level, range)), String.format("%.1f", range));
    }
}
