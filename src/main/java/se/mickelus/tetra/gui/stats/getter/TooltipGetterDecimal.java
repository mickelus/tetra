package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class TooltipGetterDecimal implements ITooltipGetter {

    protected IStatGetter statGetter;
    protected String localizationKey;

    public TooltipGetterDecimal(String localizationKey, IStatGetter statGetter) {
        this.localizationKey = localizationKey;
        this.statGetter = statGetter;
    }


    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        return I18n.format(localizationKey, String.format("%.2f", statGetter.getValue(player, itemStack)));
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
