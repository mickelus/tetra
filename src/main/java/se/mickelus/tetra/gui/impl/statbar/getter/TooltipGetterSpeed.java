package se.mickelus.tetra.gui.impl.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class TooltipGetterSpeed implements ITooltipGetter {

    private static final IStatGetter statGetter = new StatGetterSpeed();
    private static final String localizationKey = "stats.speed.tooltip";

    public TooltipGetterSpeed() { }


    @Override
    public String getTooltip(PlayerEntity player, ItemStack itemStack) {
        double speed = statGetter.getValue(player, itemStack);
        return I18n.format(localizationKey, String.format("%.2f", 1 / speed), String.format("%.2f", speed));
    }
}
