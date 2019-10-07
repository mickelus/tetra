package se.mickelus.tetra.blocks.forged.container;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiRect;
import se.mickelus.tetra.gui.GuiTexture;
import se.mickelus.tetra.gui.animation.AnimationChain;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.impl.GuiTabVerticalGroup;

import java.util.stream.IntStream;

@OnlyIn(Dist.CLIENT)
public class ForgedContainerScreen extends ContainerScreen<ForgedContainerContainer> {

    private static final String containerTexture = "textures/gui/forged-container.png";
    private static final String playerInventoryTexture = "textures/gui/player-inventory.png";

    // private final TileEntityForgedContainer tileEntity;

    private final GuiElement gui;

    private final AnimationChain slotTransition;

    private final GuiTabVerticalGroup compartmentButtons;

    public ForgedContainerScreen(ForgedContainerContainer screenContainer, PlayerInventory playerInventory, ITextComponent title) {
        super(screenContainer, playerInventory, title);

        this.xSize = 179;
        this.ySize = 176;

        // this.tileEntity = tileEntity;

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
    // todo 1.14: or should it be keyReleased? Used to be "keyTyped" before 1.14
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        // todo 1.14: used to pass a char here, none of those around so we probably need to do a conversion
        compartmentButtons.keyTyped((char) p_keyPressed_1_);
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    public void tick() {
        int size = TileEntityForgedContainer.compartmentSize;
        for (int i = 0; i < TileEntityForgedContainer.compartmentCount; i++) {
            boolean hasContent = false;
            for (int j = 0; j < size; j++) {
                if (!container.getTileEntity().getStackInSlot(i * size + j).isEmpty()) {
                    hasContent = true;
                    break;
                }
            }
            compartmentButtons.setHasContent(i, hasContent);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
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
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        return gui.onClick((int) mouseX, (int) mouseY);
    }
}
