package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.gui.GuiElement;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiForgedContainer extends GuiContainer {

    private static GuiForgedContainer instance;

    private static final String containerTexture = "textures/gui/forged-container.png";
    private static final String playerInventoryTexture = "textures/gui/player-inventory.png";

    private EntityPlayer viewingPlayer;

    private final TileEntityForgedContainer tileEntity;
    private final ContainerForgedContainer container;

    private final GuiElement gui;

    public GuiForgedContainer(ContainerForgedContainer container, TileEntityForgedContainer tileEntity, EntityPlayer viewingPlayer) {
        super(container);
        this.allowUserInput = false;
        this.xSize = 320;
        this.ySize = 240;

        this.tileEntity = tileEntity;
        this.container = container;

        this.viewingPlayer = viewingPlayer;

        gui = new GuiElement(0, 0, xSize, ySize);

        instance = this;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);

        drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        gui.draw(x, y, width, height, mouseX, mouseY, 1);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        gui.onClick(mouseX, mouseY);
    }
}
