package se.mickelus.tetra.blocks.forged.container;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiRect;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.mgui.gui.animation.AnimationChain;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.mgui.gui.impl.GuiTabVerticalGroup;
import se.mickelus.tetra.TetraMod;

import java.util.stream.IntStream;

@OnlyIn(Dist.CLIENT)
public class ForgedContainerScreen extends ContainerScreen<ForgedContainerContainer> {
    private static final ResourceLocation containerTexture = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/forged-container.png");
    private static final ResourceLocation playerInventoryTexture = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/player-inventory.png");

    private final ForgedContainerTile tileEntity;
    private final ForgedContainerContainer container;

    private final GuiElement guiRoot;

    private final AnimationChain slotTransition;

    private final GuiTabVerticalGroup compartmentButtons;

    public ForgedContainerScreen(ForgedContainerContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);

        this.xSize = 179;
        this.ySize = 176;

        this.tileEntity = container.getTile();
        this.container = container;

        guiRoot = new GuiElement(0, 0, xSize, ySize);
        guiRoot.addChild(new GuiTexture(0, -13, 179, 128, containerTexture));
        guiRoot.addChild(new GuiTexture(0, 103, 179, 106, playerInventoryTexture));

        compartmentButtons = new GuiTabVerticalGroup(10, 26, this::changeCompartment,
                IntStream.range(1, ForgedContainerTile.compartmentCount + 1)
                        .mapToObj(i -> I18n.format("tetra.forged_container.compartment", i))
                        .toArray(String[]::new));
        guiRoot.addChild(compartmentButtons);

        GuiRect slotTransitionElement = new GuiRect(12, 0, 152, 101, 0);
        slotTransitionElement.setOpacity(0);
        guiRoot.addChild(slotTransitionElement);
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
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        return guiRoot.onClick((int) mouseX, (int) mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        guiRoot.mouseReleased((int) mouseX, (int) mouseY);

        return true;
    }

    @Override
    public boolean charTyped(char typecChar, int keyCode) {
        compartmentButtons.keyTyped(typecChar);
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        int size = ForgedContainerTile.compartmentSize;
        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(itemHandler -> {
            for (int i = 0; i < ForgedContainerTile.compartmentCount; i++) {
                boolean hasContent = false;
                for (int j = 0; j < size; j++) {
                    if (!itemHandler.getStackInSlot(i * size + j).isEmpty()) {
                        hasContent = true;
                        break;
                    }
                }
                compartmentButtons.setHasContent(i, hasContent);
            }
        });
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack, 0);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        func_230459_a_(matrixStack, mouseX, mouseY);

        drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        guiRoot.draw(matrixStack, x, y, width, height, mouseX, mouseY, 1);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) { }
}
