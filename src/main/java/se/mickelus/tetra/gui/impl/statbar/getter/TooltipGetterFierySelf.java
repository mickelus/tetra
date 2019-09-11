package se.mickelus.tetra.gui.impl.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.ItemEffect;

public class TooltipGetterFierySelf implements ITooltipGetter {


    private static final IStatGetter efficiencyGetter = new StatGetterEffectEfficiency(ItemEffect.fierySelf, 100);
    private static final IStatGetter levelGetter = new StatGetterEffectLevel(ItemEffect.fierySelf, 1);

    public TooltipGetterFierySelf() { }


    @Override
    public String getTooltip(EntityPlayer player, ItemStack itemStack) {
        return I18n.format("stats.fierySelf.tooltip",
                String.format("%.2f%%", efficiencyGetter.getValue(player, itemStack)),
                String.format("%.2f", levelGetter.getValue(player, itemStack)));
    }
}
