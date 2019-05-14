package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class TooltipGetterInteger implements ITooltipGetter {

    protected IStatGetter statGetter;
    protected String localizationKey;

    public TooltipGetterInteger(String localizationKey, IStatGetter statGetter) {
        this.localizationKey = localizationKey;
        this.statGetter = statGetter;
    }


    @Override
    public String getTooltip(EntityPlayer player, ItemStack itemStack) {
        return I18n.format(localizationKey, (int) statGetter.getValue(player, itemStack));
    }
}
