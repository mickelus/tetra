package se.mickelus.tetra.gui;

import net.minecraft.resources.ResourceLocation;
import se.mickelus.tetra.TetraMod;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class GuiTextures {
    public static final ResourceLocation workbench = ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "textures/gui/workbench.png");
    public static final ResourceLocation holo = ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "textures/gui/holo.png");
    public static final ResourceLocation toolActions = ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "textures/gui/tool-actions.png");
    public static final ResourceLocation playerInventory = ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "textures/gui/player-inventory.png");
    public static final ResourceLocation toolbelt = ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "textures/gui/toolbelt-inventory.png");
    public static final ResourceLocation glyphs = ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "textures/gui/glyphs.png");
    public static final ResourceLocation hud = ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "textures/gui/hud.png");
}
