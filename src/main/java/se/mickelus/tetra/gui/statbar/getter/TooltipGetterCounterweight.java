package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

public class TooltipGetterCounterweight implements ITooltipGetter {
    private static final IStatGetter levelGetter = new StatGetterEffectLevel(ItemEffect.counterweight, 1);

    public TooltipGetterCounterweight() { }

    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        int level = (int) levelGetter.getValue(player, itemStack);
        return I18n.format("tetra.stats.counterweight.indicator_tooltip",
                String.format("%.2f", ItemModularHandheld.getCounterWeightBonus((int) levelGetter.getValue(player, itemStack),
                        ItemModularHandheld.getIntegrityCost(itemStack))), level);
    }
}
