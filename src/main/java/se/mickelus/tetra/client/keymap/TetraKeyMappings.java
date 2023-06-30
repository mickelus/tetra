package se.mickelus.tetra.client.keymap;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class TetraKeyMappings {
    public static final String bindingGroup = "tetra.binding.group";
    public static final KeyMapping accessBinding = new KeyMapping("tetra.toolbelt.binding.access", TetraKeyConflictContext.toolbelt, InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B, bindingGroup);
    public static final KeyMapping restockBinding = new KeyMapping("tetra.toolbelt.binding.restock", TetraKeyConflictContext.toolbelt, KeyModifier.SHIFT,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, bindingGroup);
    public static final KeyMapping openBinding = new KeyMapping("tetra.toolbelt.binding.open", TetraKeyConflictContext.toolbelt, KeyModifier.ALT,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, bindingGroup);

    public static final KeyMapping secondaryUseBinding = new KeyMapping("tetra.toolbelt.binding.secondary_use", TetraKeyConflictContext.secondaryInteraction,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, bindingGroup);

    enum TetraKeyConflictContext implements IKeyConflictContext {
        toolbelt {
            public boolean isActive() {
                return Minecraft.getInstance().screen == null;
            }

            public boolean conflicts(IKeyConflictContext other) {
                return this == other;
            }
        },
        secondaryInteraction {
            public boolean isActive() {
                return Minecraft.getInstance().screen == null;
            }

            public boolean conflicts(IKeyConflictContext other) {
                return this == other;
            }
        };
    }
}
