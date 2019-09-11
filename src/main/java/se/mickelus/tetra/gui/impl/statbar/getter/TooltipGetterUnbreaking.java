package se.mickelus.tetra.gui.impl.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.module.ItemEffect;

public class TooltipGetterUnbreaking implements ITooltipGetter {

    private static final IStatGetter levelGetter = new StatGetterEffectLevel(ItemEffect.unbreaking, 1);
    private static final IStatGetter chanceGetter = new StatGetterUnbreaking();

    public TooltipGetterUnbreaking() { }


    @Override
    public String getTooltip(EntityPlayer player, ItemStack itemStack) {
        return I18n.format("stats.unbreaking.tooltip",
                String.format("%d", (int) levelGetter.getValue(player, itemStack)),
                String.format("%.2f", chanceGetter.getValue(player, itemStack)));
    }
}
