package se.mickelus.tetra.gui;

import net.minecraft.resources.ResourceLocation;
import se.mickelus.tetra.TetraMod;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GuiTextures {
    public static final ResourceLocation workbench = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/workbench.png");
    public static final ResourceLocation playerInventory = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/player-inventory.png");
    public static final ResourceLocation toolbelt = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/toolbelt-inventory.png");
    public static final ResourceLocation glyphs = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/glyphs.png");
    public static final ResourceLocation hud = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/hud.png");
}
