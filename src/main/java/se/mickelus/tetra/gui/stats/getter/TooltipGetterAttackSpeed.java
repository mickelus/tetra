package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TooltipGetterAttackSpeed implements ITooltipGetter {

    private static final String localizationKey = "tetra.stats.speed.tooltip";
    private final IStatGetter statGetter;

    public TooltipGetterAttackSpeed(IStatGetter statGetter) {
        this.statGetter = statGetter;
    }


    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        double speed = statGetter.getValue(player, itemStack);
        return I18n.get(localizationKey, String.format("%.2f", 1 / speed), String.format("%.2f", speed * 0.5 + 0.5));
    }

    @Override
    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return true;
    }

    @Override
    public String getTooltipExtension(Player player, ItemStack itemStack) {
        return I18n.get("tetra.stats.speed.tooltip_extended");
    }
}
