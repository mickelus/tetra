package se.mickelus.tetra.items.toolbelt.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.items.toolbelt.ContainerToolbelt;
import se.mickelus.tetra.items.toolbelt.OverlayToolbelt;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiToolbelt extends GuiContainer {

    private static GuiToolbelt instance;

    private static final ResourceLocation TOOLBELT_TEXTURE = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/toolbelt-inventory.png");
    private static final ResourceLocation INVENTORY_TEXTURE = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/player-inventory.png");

    private GuiElement defaultGui;

    public GuiToolbelt(ContainerToolbelt container) {
        super(container);
        this.allowUserInput = false;
        this.xSize = 179;
        this.ySize = 176;

        int numSlots = container.getToolbeltInventory().getSizeInventory();

        defaultGui = new GuiElement(0, 0, xSize, ySize);

        // inventory background
        defaultGui.addChild(new GuiTexture(0, 103, 179, 106, INVENTORY_TEXTURE));

        // toolbelt background rects
        defaultGui.addChild(new GuiRect(-8 * numSlots + 85, 16, numSlots * 17 - 1, 22, 0xff000000));
        defaultGui.addChild(new GuiRect(-8 * numSlots + 85, 17, numSlots * 17 - 1, 20, 0xffffffff));
        defaultGui.addChild(new GuiRect(-8 * numSlots + 85, 18, numSlots * 17 - 1, 18, 0xff000000));

        // toolbelt left cap
        defaultGui.addChild(new GuiTexture(-8 * numSlots + 72, 14, 17, 28, TOOLBELT_TEXTURE));

        // toolbelt right cap
        defaultGui.addChild(new GuiTexture(9 * numSlots + 80, 13, 17, 28, 17, 0, TOOLBELT_TEXTURE));

        defaultGui.addChild(new GuiKeybinding(166, 85, OverlayToolbelt.instance.accessBinding));
        defaultGui.addChild(new GuiKeybinding(166, 100, OverlayToolbelt.instance.restockBinding));

        instance = this;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        defaultGui.draw(x, y, width, height, mouseX, mouseY, 1);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // todo: clear shadow slot if rightclick and "normal" slot is empty
    }
}
