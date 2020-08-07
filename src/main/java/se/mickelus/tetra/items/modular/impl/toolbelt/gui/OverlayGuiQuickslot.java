package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import se.mickelus.mgui.gui.GuiItem;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;

public class OverlayGuiQuickslot extends GuiElement {

    public static final int height = 20;

    private ItemStack itemStack;

    private int slot;

    private Minecraft mc;

    private KeyframeAnimation showAnimation;

    private FontRenderer fontRenderer;

    private GuiItem guiItem;

    public OverlayGuiQuickslot(int x, int y, ItemStack itemStack, int slot) {
        super(x, y, 200, height);

        this.itemStack = itemStack;
        this.slot = slot;

        mc = Minecraft.getInstance();

        if (itemStack != null) {
            fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
        }

        if (fontRenderer == null) {
            fontRenderer = mc.fontRenderer;
        }

        guiItem = new GuiItem(38, 1);
        guiItem.setOpacity(0);
        guiItem.setItem(itemStack);
        addChild(guiItem);

        isVisible = false;
        opacity = 0;
        showAnimation = new KeyframeAnimation(500, guiItem)
            .applyTo(new Applier.Opacity(1), new Applier.TranslateY(-1, 1))
            .withDelay(slot * 100);

    }

    @Override
    protected void onShow() {
        showAnimation.start();
    }

    @Override
    protected boolean onHide() {
        if (showAnimation.isActive()) {
            showAnimation.stop();
        }
        opacity = 0;
        return true;
    }

    @Override
    public void draw(MatrixStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);
        if (opacity * guiItem.getOpacity() < 1) {
            drawOverlay(matrixStack, refX + x + guiItem.getX(), refY + y + guiItem.getY(), opacity * guiItem.getOpacity());
        }

        if (hasFocus()) {
            fontRenderer.drawStringWithShadow(matrixStack, itemStack.getDisplayName().getString(), x + refX + 63, y + refY + 6, GuiColors.hover);
        }
    }

    private void drawOverlay(MatrixStack matrixStack, int x, int y, float opacity) {
        drawRect(matrixStack, x - 1, y - 1, x + 17, y + 17, 0, 1 - opacity);
    }


    public int getSlot() {
        return slot;
    }
}
