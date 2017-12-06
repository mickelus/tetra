package se.mickelus.tetra.items.toolbelt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.GuiElement;

public class OverlayGuiItem extends GuiElement {

    private ItemStack itemStack;

    private int slot;

    private Minecraft mc;

    private static final int COLOR_DEFAULT = 0xffffffff;
    private static final int COLOR_HOVER = 0xffffff00;

    public OverlayGuiItem(int x, int y, ItemStack itemStack, int slot) {
        super(x, y, 200, 20);

        this.itemStack = itemStack;
        this.slot = slot;

        mc = Minecraft.getMinecraft();
    }

    @Override
    public void draw(int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        super.draw(refX, refY, screenWidth, screenHeight, mouseX, mouseY);

        mc.fontRenderer.drawStringWithShadow(itemStack.getDisplayName(), x + refX + 20, y + refY + 6, hasFocus() ? COLOR_HOVER : COLOR_DEFAULT);

        drawItemStack(itemStack, x + refX, y + refY, null);
    }

    private void drawItemStack(ItemStack stack, int x, int y, String altText) {
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderHelper.enableGUIStandardItemLighting();

        // GlStateManager.translate(0.0F, 0.0F, 32.0F);
        this.zLevel = 200.0F;
        mc.getRenderItem().zLevel = 200.0F;
        FontRenderer font = null;

        if (stack != null) {
            font = stack.getItem().getFontRenderer(stack);
        }

        if (font == null) {
            font = mc.fontRenderer;
        }

        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        mc.getRenderItem().renderItemOverlayIntoGUI(font, stack, x, y, altText);

        this.zLevel = 0.0F;
        mc.getRenderItem().zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
    }


    public int getSlot() {
        return slot;
    }
}
