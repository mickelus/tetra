package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TooltipGetterUnbreaking implements ITooltipGetter {

    private final IStatGetter levelGetter;
    private final IStatGetter chanceGetter;

    public TooltipGetterUnbreaking(IStatGetter levelGetter) {
        this.levelGetter = levelGetter;
        this.chanceGetter = new StatGetterUnbreaking(levelGetter);
    }


    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        return I18n.get("tetra.stats.unbreaking.tooltip",
                String.format("%d", (int) levelGetter.getValue(player, itemStack)),
                String.format("%.2f", chanceGetter.getValue(player, itemStack)));
    }
}
