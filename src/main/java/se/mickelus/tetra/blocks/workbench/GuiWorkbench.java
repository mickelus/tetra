package se.mickelus.tetra.blocks.workbench;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.*;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiWorkbench extends GuiContainer {

    private static GuiWorkbench instance;

    private static final String WORKBENCH_TEXTURE = "textures/gui/workbench.png";
    private static final String INVENTORY_TEXTURE = "textures/gui/player-inventory.png";
    private final TileEntityWorkbench tileEntity;


    private GuiElement defaultGui;

    private GuiElement actionLabels;


    public GuiWorkbench(ContainerWorkbench container, TileEntityWorkbench tileEntity) {
        super(container);
        this.allowUserInput = false;
        this.xSize = 179;
        this.ySize = 176;

        this.tileEntity = tileEntity;

        defaultGui = new GuiElement(0, 0, xSize, ySize);
        defaultGui.addChild(new GuiTextureOffset(61, 0, 51, 51, WORKBENCH_TEXTURE));
        defaultGui.addChild(new GuiTexture(0, 74, 179, 106, INVENTORY_TEXTURE));

        actionLabels = new GuiElement(0, 0, 0, 0);
        actionLabels.addChild(new GuiString(30, -15, "Upgrade"));
        actionLabels.addChild(new GuiString(30, -5, "Salvage"));
        actionLabels.addChild(new GuiString(30, 5, "Repair"));
        actionLabels.addChild(new GuiString(30, 15, "Enchant"));
    }

    /**
     * Draws the background layer of this container (behind the items).
     *
     * @param partialTicks How far into the current tick the game is, with 0.0 being the start of the tick and 1.0 being
     * the end.
     * @param mouseX Mouse x coordinate
     * @param mouseY Mouse y coordinate
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        defaultGui.draw(x, y, width, height, mouseX, mouseY);
        actionLabels.draw(width / 2, y, width, height, mouseX, mouseY);

        this.fontRendererObj.drawString("" + tileEntity.getCurrentState(), x, y, 0xffffffff, false);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        tileEntity.setCurrentState(tileEntity.getCurrentState() + 1);
    }
}
