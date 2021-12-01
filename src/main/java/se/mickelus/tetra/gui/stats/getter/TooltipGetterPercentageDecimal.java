package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TooltipGetterPercentageDecimal implements ITooltipGetter {

    protected IStatGetter statGetter;
    protected String localizationKey;

    public TooltipGetterPercentageDecimal(String localizationKey, IStatGetter statGetter) {
        this.localizationKey = localizationKey;
        this.statGetter = statGetter;
    }


    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        return I18n.get(localizationKey, String.format("%.1f%%", statGetter.getValue(player, itemStack)));
    }

    @Override
    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return I18n.exists(localizationKey + "_extended");
    }

    @Override
    public String getTooltipExtension(Player player, ItemStack itemStack) {
        return I18n.get(localizationKey + "_extended");
    }
}
