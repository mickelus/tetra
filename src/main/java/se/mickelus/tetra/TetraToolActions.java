package se.mickelus.tetra;

import net.minecraftforge.common.ToolAction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TetraToolActions {
    // todo 1.12: replace cut with vanilla SWORD_DIG
    public static final ToolAction cut = ToolAction.get("cut");
    public static final ToolAction hammer = ToolAction.get("hammer_dig");
    public static final ToolAction pry = ToolAction.get("pry");
    public static final ToolAction dowse = ToolAction.get("dowse");
}
