package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ItemModularHandheld;

public class TooltipGetterCounterweight implements ITooltipGetter {
    private static final IStatGetter levelGetter = new StatGetterEffectLevel(ItemEffect.counterweight, 1);

    public TooltipGetterCounterweight() { }

    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        int level = (int) levelGetter.getValue(player, itemStack);
        return I18n.get("tetra.stats.counterweight.indicator_tooltip",
                String.format("%.2f", ItemModularHandheld.getCounterWeightBonus((int) levelGetter.getValue(player, itemStack),
                        IModularItem.getIntegrityCost(itemStack))), level);
    }
}
