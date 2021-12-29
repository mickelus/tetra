package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.effect.ItemEffect;

public class TooltipGetterUnbreaking implements ITooltipGetter {

    private final IStatGetter levelGetter;
    private final IStatGetter chanceGetter;

    public TooltipGetterUnbreaking(IStatGetter levelGetter) {
        this.levelGetter = levelGetter;
        this.chanceGetter = new StatGetterUnbreaking(levelGetter);
    }


    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        return I18n.format("tetra.stats.unbreaking.tooltip",
                String.format("%d", (int) levelGetter.getValue(player, itemStack)),
                String.format("%.2f", chanceGetter.getValue(player, itemStack)));
    }
}
