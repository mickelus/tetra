package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.gui.GuiButton;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiRect;
import se.mickelus.tetra.gui.GuiTexture;
import se.mickelus.tetra.gui.animation.AnimationChain;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiForgedContainer extends GuiContainer {

    private static final String containerTexture = "textures/gui/forged-container.png";
    private static final String playerInventoryTexture = "textures/gui/player-inventory.png";

    private EntityPlayer viewingPlayer;

    private final TileEntityForgedContainer tileEntity;
    private final ContainerForgedContainer container;

    private final GuiElement gui;

    private final AnimationChain slotTransition;

    private final GuiCompartmentButton[] compartmentButtons;

    private final char[] compartmentKeybinds = new char[] { 'a', 's', 'd' };

    public GuiForgedContainer(ContainerForgedContainer container, TileEntityForgedContainer tileEntity, EntityPlayer viewingPlayer) {
        super(container);
        this.allowUserInput = false;
        this.xSize = 179;
        this.ySize = 176;

        this.tileEntity = tileEntity;
        this.container = container;

        this.viewingPlayer = viewingPlayer;

        gui = new GuiElement(0, 0, xSize, ySize);
        gui.addChild(new GuiTexture(0, -13, 179, 128, containerTexture));
        gui.addChild(new GuiTexture(0, 103, 179, 106, playerInventoryTexture));

        compartmentButtons = new GuiCompartmentButton[TileEntityForgedContainer.compartmentCount];
        for (int i = 0; i < compartmentButtons.length; i++) {
            final int index = i;
            compartmentButtons[i] = new GuiCompartmentButton(11, 27 + 16 * i, i,
                    Character.toString(compartmentKeybinds[i]), () -> changeCompartment(index));
            gui.addChild(compartmentButtons[i]);
        }

        GuiRect slotTransitionElement = new GuiRect(12, 0, 152, 101, 0);
        slotTransitionElement.setOpacity(0);
        gui.addChild(slotTransitionElement);
        slotTransition = new AnimationChain(
                new KeyframeAnimation(30, slotTransitionElement).applyTo(new Applier.Opacity(0.3f)),
                new KeyframeAnimation(50, slotTransitionElement).applyTo(new Applier.Opacity(0)));
    }

    private void changeCompartment(int index) {
        container.changeCompartment(index);
        for (int i = 0; i < compartmentButtons.length; i++) {
            compartmentButtons[i].setActive(i == index);
        }
        slotTransition.stop();
        slotTransition.start();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        for (int i = 0; i < compartmentKeybinds.length; i++) {
            if (typedChar == compartmentKeybinds[i]) {
                changeCompartment(i);
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        int size = TileEntityForgedContainer.compartmentSize;
        for (int i = 0; i < compartmentButtons.length; i++) {
            boolean hasContent = false;
            for (int j = 0; j < size; j++) {
                if (!tileEntity.getStackInSlot(i * size + j).isEmpty()) {
                    hasContent = true;
                    break;
                }
            }
            compartmentButtons[i].setHasContent(hasContent);
        }
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
