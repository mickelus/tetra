package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;

public class TooltipGetterTool implements ITooltipGetter {

    private final IStatGetter levelGetter;
    private final IStatGetter efficiencyGetter;
    private final String localizationKey;


    public TooltipGetterTool(ToolType tool) {
        localizationKey = "tetra.stats." + tool.getName() + ".tooltip";

        levelGetter = new StatGetterToolLevel(tool);
        efficiencyGetter = new StatGetterToolEfficiency(tool);
    }


    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        return I18n.format(localizationKey,
                (int) levelGetter.getValue(player, itemStack),
                String.format("%.2f", efficiencyGetter.getValue(player, itemStack)));
    }

    public boolean hasExtendedTooltip(PlayerEntity player, ItemStack itemStack) {
        return true;
    }

    public String getTooltipExtension(PlayerEntity player, ItemStack itemStack) {
        return I18n.format("tetra.stats.tool.tooltip_extended");
    }
}
