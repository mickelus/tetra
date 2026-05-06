package se.mickelus.tetra;

import net.neoforged.neoforge.common.ItemAbility;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TetraItemAbilities {
    // todo 1.12: replace cut with vanilla SWORD_DIG
    public static final ItemAbility cut = ItemAbility.get("cut");
    public static final ItemAbility hammer = ItemAbility.get("hammer_dig");
    public static final ItemAbility pry = ItemAbility.get("pry");
    public static final ItemAbility dowse = ItemAbility.get("dowse");
}
