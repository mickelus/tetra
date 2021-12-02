package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class TooltipGetterTool implements ITooltipGetter {

    private final IStatGetter levelGetter;
    private final IStatGetter efficiencyGetter;
    private final String localizationKey;


    public TooltipGetterTool(ToolAction tool) {
        localizationKey = "tetra.stats." + tool.name() + ".tooltip";

        levelGetter = new StatGetterToolLevel(tool);
        efficiencyGetter = new StatGetterToolEfficiency(tool);
    }


    @Override
    public String getTooltipBase(Player player, ItemStack itemStack) {
        return I18n.get(localizationKey,
                (int) levelGetter.getValue(player, itemStack),
                String.format("%.2f", efficiencyGetter.getValue(player, itemStack)));
    }

    public boolean hasExtendedTooltip(Player player, ItemStack itemStack) {
        return true;
    }

    public String getTooltipExtension(Player player, ItemStack itemStack) {
        return I18n.get("tetra.stats.tool.tooltip_extended");
    }
}
