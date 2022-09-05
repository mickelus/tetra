package se.mickelus.tetra.items.modular.impl.toolbelt;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class ToolbeltKeyMappings {
    public static final String bindingGroup = "tetra.toolbelt.binding.group";
    public static final KeyMapping accessBinding = new KeyMapping("tetra.toolbelt.binding.access", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B, bindingGroup);
    public static final KeyMapping restockBinding = new KeyMapping("tetra.toolbelt.binding.restock", KeyConflictContext.IN_GAME, KeyModifier.SHIFT,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, bindingGroup);
    public static final KeyMapping openBinding = new KeyMapping("tetra.toolbelt.binding.open", KeyConflictContext.IN_GAME, KeyModifier.ALT,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, bindingGroup);
}
