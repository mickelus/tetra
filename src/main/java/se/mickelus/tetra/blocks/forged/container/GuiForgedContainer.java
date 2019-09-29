package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiRect;
import se.mickelus.tetra.gui.GuiTexture;
import se.mickelus.tetra.gui.animation.AnimationChain;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.impl.GuiTabVerticalGroup;

import java.io.IOException;
import java.util.stream.IntStream;

@SideOnly(Side.CLIENT)
public class GuiForgedContainer extends GuiContainer {

    private static final String containerTexture = "textures/gui/forged-container.png";
    private static final String playerInventoryTexture = "textures/gui/player-inventory.png";

    private final TileEntityForgedContainer tileEntity;
    private final ContainerForgedContainer container;

    private final GuiElement gui;

    private final AnimationChain slotTransition;

    private final GuiTabVerticalGroup compartmentButtons;

    public GuiForgedContainer(ContainerForgedContainer container, TileEntityForgedContainer tileEntity) {
        super(container);
        this.allowUserInput = false;
        this.xSize = 179;
        this.ySize = 176;

        this.tileEntity = tileEntity;
        this.container = container;

        gui = new GuiElement(0, 0, xSize, ySize);
        gui.addChild(new GuiTexture(0, -13, 179, 128, containerTexture));
        gui.addChild(new GuiTexture(0, 103, 179, 106, playerInventoryTexture));

        compartmentButtons = new GuiTabVerticalGroup(10, 26, this::changeCompartment,
                IntStream.range(1, TileEntityForgedContainer.compartmentCount + 1)
                        .mapToObj(i -> I18n.format("forged_container.compartment", i))
                        .toArray(String[]::new));
        gui.addChild(compartmentButtons);

        GuiRect slotTransitionElement = new GuiRect(12, 0, 152, 101, 0);
        slotTransitionElement.setOpacity(0);
        gui.addChild(slotTransitionElement);
        slotTransition = new AnimationChain(
                new KeyframeAnimation(30, slotTransitionElement).applyTo(new Applier.Opacity(0.3f)),
                new KeyframeAnimation(50, slotTransitionElement).applyTo(new Applier.Opacity(0)));
    }

    private void changeCompartment(int index) {
        container.changeCompartment(index);
        compartmentButtons.setActive(index);
        slotTransition.stop();
        slotTransition.start();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        compartmentButtons.keyTyped(typedChar);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        int size = TileEntityForgedContainer.compartmentSize;
        for (int i = 0; i < TileEntityForgedContainer.compartmentCount; i++) {
            boolean hasContent = false;
            for (int j = 0; j < size; j++) {
                if (!tileEntity.getStackInSlot(i * size + j).isEmpty()) {
                    hasContent = true;
                    break;
                }
            }
            compartmentButtons.setHasContent(i, hasContent);
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
