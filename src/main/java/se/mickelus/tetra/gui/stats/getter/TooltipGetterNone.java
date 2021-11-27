package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class TooltipGetterNone implements ITooltipGetter {
    protected String localizationKey;

    public TooltipGetterNone(String localizationKey) {
        this.localizationKey = localizationKey;
    }


    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        return I18n.format(localizationKey);
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
