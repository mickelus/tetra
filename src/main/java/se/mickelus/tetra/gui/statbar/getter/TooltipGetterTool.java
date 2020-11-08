package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.effect.ItemEffect;

public class TooltipGetterTool implements ITooltipGetter {

    private final IStatGetter levelGetter;
    private final IStatGetter efficiencyGetter;
    private final String localizationKey;

    private static final String strikingKey = "tetra.stats.tool.striking";
    private IStatGetter strikingGetter;

    private static final String sweepingKey = "tetra.stats.tool.sweeping";
    private IStatGetter sweepingGetter;

    public TooltipGetterTool(ToolType tool) {
        localizationKey = "tetra.stats." + tool.getName() + ".tooltip";

        levelGetter = new StatGetterToolLevel(tool);
        efficiencyGetter = new StatGetterToolEfficiency(tool);

        if (tool == ToolType.AXE) {
            strikingGetter = new StatGetterEffectLevel(ItemEffect.strikingAxe, 1);
        } else if (tool == ToolType.PICKAXE) {
            strikingGetter = new StatGetterEffectLevel(ItemEffect.strikingPickaxe, 1);
        } else if (tool == ToolTypes.cut) {
            strikingGetter = new StatGetterEffectLevel(ItemEffect.strikingCut, 1);
        } else if (tool == ToolType.SHOVEL) {
            strikingGetter = new StatGetterEffectLevel(ItemEffect.strikingShovel, 1);
        } else if (tool == ToolType.HOE) {
            strikingGetter = new StatGetterEffectLevel(ItemEffect.strikingHoe, 1);
        }

        sweepingGetter = new StatGetterEffectLevel(ItemEffect.sweepingStrike, 1);
    }


    @Override
    public String getTooltipBase(PlayerEntity player, ItemStack itemStack) {
        String modifier = "";

        if (strikingGetter != null && strikingGetter.getValue(player, itemStack) > 0) {
            if (sweepingGetter.getValue(player, itemStack) > 0) {
                modifier = " \n" + I18n.format(sweepingKey) + "\n ";
            } else {
                modifier = " \n" + I18n.format(strikingKey) + "\n ";
            }
        }

        return I18n.format(localizationKey,
                modifier,
                (int) levelGetter.getValue(player, itemStack),
                String.format("%.2f", efficiencyGetter.getValue(player, itemStack)));
    }
}
