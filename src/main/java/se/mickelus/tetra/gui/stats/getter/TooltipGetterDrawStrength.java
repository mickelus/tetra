package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TooltipGetterDrawStrength implements ITooltipGetter {
    private final IStatGetter getter;

    public TooltipGetterDrawStrength(IStatGetter getter) {
        this.getter = getter;
    }

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        double drawStrength = getter.getValue(player, itemStack);
        return I18n.get("tetra.stats.draw_strength.tooltip",
                String.format("%.1f", drawStrength),
                String.format("%.1f", 1.5 * drawStrength + 1), // max damage including "crit" bonus is this
                String.format("%.1f", 3 * ModularBowItem.getArrowVelocity(20, drawStrength, 0, false)));
    }

    @Override
    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return true;
    }

    @Override
    public String getTooltipExtension(Player player, ItemStack itemStack) {
        return I18n.get("tetra.stats.draw_strength.tooltip_extended");
    }
}
