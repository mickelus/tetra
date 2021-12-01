package se.mickelus.tetra;

import net.minecraftforge.common.ToolAction;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class ToolTypes {
    public static final ToolAction cut = ToolAction.get("cut");
    public static final ToolAction hammer = ToolAction.get("hammer");
    public static final ToolAction pry = ToolAction.get("pry");
}
