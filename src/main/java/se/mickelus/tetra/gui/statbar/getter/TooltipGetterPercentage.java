package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class TooltipGetterPercentage implements ITooltipGetter {

    protected IStatGetter statGetter;
    protected String localizationKey;

    public TooltipGetterPercentage(String localizationKey, IStatGetter statGetter) {
        this.localizationKey = localizationKey;
        this.statGetter = statGetter;
    }


    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        return I18n.format(localizationKey, String.format("%.1f%%", statGetter.getValue(player, itemStack)));
    }

    @Override
    public boolean hasExtendedTooltip(PlayerEntity player, ItemStack itemStack) {
        return I18n.hasKey(localizationKey + "_extended");
    }

    @Override
    public String getTooltipExtension(PlayerEntity player, ItemStack itemStack) {
        return I18n.format(localizationKey + "_extended");
    }
}
