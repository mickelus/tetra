package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.ConfigHandler;

public class TooltipGetterReach implements ITooltipGetter {
    private final IStatGetter getter;

    public TooltipGetterReach(IStatGetter getter) {
        this.getter = getter;
    }

    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        double reach = getter.getValue(player, itemStack);
        if (ConfigHandler.enableReach.get()) {
            return I18n.get("tetra.stats.reach.tooltip", String.format("%.1f", reach), String.format("%.1f", reach - 1.5));
        }

        return I18n.get("tetra.stats.reach.tooltip_block", String.format("%.1f", reach));
    }
}
