package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class TooltipGetterMultiValue implements ITooltipGetter {

    protected IStatGetter[] statGetter;
    protected StatFormat[] formatters;
    protected String localizationKey;

    public TooltipGetterMultiValue(String localizationKey, IStatGetter[] statGetters, StatFormat[] formatters) {
        this.localizationKey = localizationKey;
        this.statGetter = statGetters;
        this.formatters = formatters;

        if (statGetters.length != formatters.length) {
            throw new RuntimeException(String.format("Mismatching length of stat getters and formatters for '%s', gett: %d, form: %d", localizationKey,
                    statGetters.length, formatters.length));
        }
    }


    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        Object[] values = new String[statGetter.length];
        for (int i = 0; i < statGetter.length; i++) {
            values[i] = formatters[i].get(statGetter[i].getValue(player, itemStack));
        }
        return I18n.format(localizationKey, values);
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
