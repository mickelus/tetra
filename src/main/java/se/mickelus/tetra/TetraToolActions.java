package se.mickelus.tetra;

import net.minecraftforge.common.ToolAction;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class TetraToolActions {
    // todo 1.18: add dig, craft, salvage variants here
    public static final ToolAction cut = ToolAction.get("cut");
    public static final ToolAction hammer = ToolAction.get("hammer_dig");
    public static final ToolAction pry = ToolAction.get("pry");
}
