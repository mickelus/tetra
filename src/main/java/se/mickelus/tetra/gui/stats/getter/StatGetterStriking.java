package se.mickelus.tetra.gui.stats.getter;

import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import se.mickelus.tetra.TetraToolActions;
import se.mickelus.tetra.effect.ItemEffect;

public class StatGetterStriking extends StatGetterEffectLevel {
    public StatGetterStriking(ToolAction toolAction) {
        super(getEffect(toolAction), 1);
    }

    static ItemEffect getEffect(ToolAction toolAction) {
        if (toolAction == ToolActions.AXE_DIG) {
            return ItemEffect.strikingAxe;
        } else if (toolAction == ToolActions.PICKAXE_DIG) {
            return ItemEffect.strikingPickaxe;
        } else if (toolAction == TetraToolActions.cut) {
            return ItemEffect.strikingCut;
        } else if (toolAction == ToolActions.SHOVEL_DIG) {
            return ItemEffect.strikingShovel;
        } else if (toolAction == ToolActions.HOE_DIG) {
            return ItemEffect.strikingHoe;
        }
        return ItemEffect.strikingPickaxe;
    }
}
