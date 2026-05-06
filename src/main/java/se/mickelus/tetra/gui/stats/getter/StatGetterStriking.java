package se.mickelus.tetra.gui.stats.getter;

import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import se.mickelus.tetra.TetraItemAbilities;
import se.mickelus.tetra.effect.ItemEffect;

public class StatGetterStriking extends StatGetterEffectLevel {
    public StatGetterStriking(ItemAbility toolAction) {
        super(getEffect(toolAction), 1);
    }

    static ItemEffect getEffect(ItemAbility toolAction) {
        if (toolAction == ItemAbilities.AXE_DIG) {
            return ItemEffect.strikingAxe;
        } else if (toolAction == ItemAbilities.PICKAXE_DIG) {
            return ItemEffect.strikingPickaxe;
        } else if (toolAction == TetraItemAbilities.cut) {
            return ItemEffect.strikingCut;
        } else if (toolAction == ItemAbilities.SHOVEL_DIG) {
            return ItemEffect.strikingShovel;
        } else if (toolAction == ItemAbilities.HOE_DIG) {
            return ItemEffect.strikingHoe;
        }
        return ItemEffect.strikingPickaxe;
    }
}
