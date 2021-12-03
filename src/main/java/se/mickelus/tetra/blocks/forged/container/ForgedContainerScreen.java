package se.mickelus.tetra.blocks.forged.container;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiRect;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.mutil.gui.animation.AnimationChain;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.VerticalTabGroupGui;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.IntStream;
@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class ForgedContainerScreen extends AbstractContainerScreen<ForgedContainerContainer> {
    private static final ResourceLocation containerTexture = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/forged-container.png");

    private final ForgedContainerTile tileEntity;
    private final ForgedContainerContainer container;

    private final GuiElement guiRoot;

    private final AnimationChain slotTransition;

    private final VerticalTabGroupGui compartmentButtons;

    public ForgedContainerScreen(ForgedContainerContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);

        this.imageWidth = 179;
        this.imageHeight = 176;

        this.tileEntity = container.getTile();
        this.container = container;

        guiRoot = new GuiElement(0, 0, imageWidth, imageHeight);
        guiRoot.addChild(new GuiTexture(0, -13, 179, 128, containerTexture));
        guiRoot.addChild(new GuiTexture(0, 103, 179, 106, GuiTextures.playerInventory));

        compartmentButtons = new VerticalTabGroupGui(10, 26, this::changeCompartment, containerTexture, 0, 128,
                IntStream.range(0, ForgedContainerTile.compartmentCount)
                        .mapToObj(i -> I18n.get("tetra.forged_container.compartment_" + i))
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        return guiRoot.onMouseClick((int) mouseX, (int) mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);

        guiRoot.onMouseRelease((int) mouseX, (int) mouseY, button);

        return true;
    }

    @Override
    public boolean charTyped(char typecChar, int keyCode) {
        compartmentButtons.keyTyped(typecChar);
        return false;
    }

    @Override
    public void containerTick() {
        super.containerTick();

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
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack, 0);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        renderTooltip(matrixStack, mouseX, mouseY);

        renderLabels(matrixStack, mouseX, mouseY);
    }


    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiRoot.draw(matrixStack, x, y, width, height, mouseX, mouseY, 1);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int x, int y) { }
}
