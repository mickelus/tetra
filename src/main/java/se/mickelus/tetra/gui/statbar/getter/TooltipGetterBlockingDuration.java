package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.ItemEffect;

public class TooltipGetterBlockingDuration implements ITooltipGetter {
    private IStatGetter durationGetter;

    private static final String reflectKey = "tetra.stats.blocking_reflect.tooltip";
    private IStatGetter reflectLevelGetter;
    private IStatGetter reflectEfficiencyGetter;

    public TooltipGetterBlockingDuration(IStatGetter durationGetter) {
        this.durationGetter = durationGetter;

        reflectLevelGetter = new StatGetterEffectLevel(ItemEffect.blockingReflect, 1);
        reflectEfficiencyGetter = new StatGetterEffectEfficiency(ItemEffect.blockingReflect, 1);
    }

    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        String modifier = "";

        double reflectLevel = reflectLevelGetter.getValue(player, itemStack);
        if (reflectLevel > 0) {
            modifier = "\n\n" + I18n.format(reflectKey, String.format("%.0f%%", reflectLevel),
                    String.format("%.0f%%", reflectEfficiencyGetter.getValue(player, itemStack) * 100));
        }

        if (durationGetter.getValue(player, itemStack) < ItemModularHandheld.blockingDurationLimit) {
            return I18n.format("tetra.stats.blocking_duration.tooltip", durationGetter.getValue(player, itemStack)) + modifier;
        }
        return I18n.format("tetra.stats.blocking.tooltip") + modifier;
    }

    @Override
    public String getTooltip(PlayerEntity player, ItemStack itemStack) {
        return getTooltipBase(player, itemStack) + "\n\n" + Tooltips.expand.getFormattedText();
    }

    @Override
    public String getTooltipExtended(PlayerEntity player, ItemStack itemStack) {
        return getTooltipBase(player, itemStack) + "\n\n" + Tooltips.expanded.getFormattedText() + "\n"
                + TextFormatting.DARK_GRAY + I18n.format("tetra.stats.blocking.tooltip_extended");
    }
}
