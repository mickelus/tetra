package se.mickelus.tetra.gui.impl.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.ItemEffect;

public class TooltipGetterArthropod implements ITooltipGetter {

    private static final IStatGetter levelGetter = new StatGetterEffectLevel(ItemEffect.arthropod, 1);

    public TooltipGetterArthropod() { }


    @Override
    public String getTooltip(EntityPlayer player, ItemStack itemStack) {
        return I18n.format("stats.arthropod.tooltip",
                String.format("%.2f", levelGetter.getValue(player, itemStack) * 2.5),
                String.format("%.2f", levelGetter.getValue(player, itemStack) * 0.5));
    }
}
