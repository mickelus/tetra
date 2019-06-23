package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

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
    public String getTooltip(EntityPlayer player, ItemStack itemStack) {
        if (absolute) {
            return I18n.format(localizationKey, (int) Math.abs(statGetter.getValue(player, itemStack)));
        }
        return I18n.format(localizationKey, (int) statGetter.getValue(player, itemStack));
    }
}
