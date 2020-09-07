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

    private final TooltipGetterBlockingReflect reflectTooltip = new TooltipGetterBlockingReflect();

    public TooltipGetterBlockingDuration(IStatGetter durationGetter) {
        this.durationGetter = durationGetter;

        reflectLevelGetter = new StatGetterEffectLevel(ItemEffect.blockingReflect, 1);
    }

    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        String modifier = "";

        double reflectLevel = reflectLevelGetter.getValue(player, itemStack);
        if (reflectLevel > 0) {
            modifier = "\n \n"
                    + TextFormatting.YELLOW + I18n.format("tetra.stats.blocking_reflect")
                    + "\n"
                    + TextFormatting.GRAY + reflectTooltip.getTooltipBase(player, itemStack).replace(TextFormatting.RESET.toString(), TextFormatting.GRAY.toString())
                    + "\n";
        }

        if (durationGetter.getValue(player, itemStack) < ItemModularHandheld.blockingDurationLimit) {
            return I18n.format("tetra.stats.blocking_duration.tooltip", durationGetter.getValue(player, itemStack)) + modifier;
        }
        return I18n.format("tetra.stats.blocking.tooltip") + modifier;
    }

    @Override
    public boolean hasExtendedTooltip(PlayerEntity player, ItemStack itemStack) {
        return true;
    }

    @Override
    public String getTooltipExtension(PlayerEntity player, ItemStack itemStack) {
        return I18n.format("tetra.stats.blocking.tooltip_extended");
    }
}
