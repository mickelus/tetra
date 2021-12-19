package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TooltipGetterInteger implements ITooltipGetter {

    protected IStatGetter statGetter;
    protected String localizationKey;

    protected boolean absolute = false;

    public TooltipGetterInteger(String localizationKey, IStatGetter statGetter, boolean absolute) {
        this(localizationKey, statGetter);

        this.absolute = absolute;
    }

    public TooltipGetterInteger(String localizationKey, IStatGetter statGetter) {
        this.localizationKey = localizationKey;
        this.statGetter = statGetter;
    }


    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        if (absolute) {
            return I18n.get(localizationKey, Math.round(Math.abs(statGetter.getValue(player, itemStack))));
        }
        return I18n.get(localizationKey, Math.round(statGetter.getValue(player, itemStack)));
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
