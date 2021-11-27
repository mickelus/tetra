package se.mickelus.tetra.blocks.workbench.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiStringSmall;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.IModularItem;

import java.util.Collections;
import java.util.List;

public class GuiIntegrityBar extends GuiElement {

    private int segmentWidth = 8;
    private static final int segmentHeight = 2;
    private static final int segmentOffset = 6;

    private static final int gainColor = 0x22ffffff;
    private static final float gainOpacity = 0.15f;
    private static final int costColor = 0xffffffff;
    private static final int overuseColor = 0x88ff5555;
    private static final float overuseOpacity = 0.55f;

    private int integrityGain;
    private int integrityCost;

    private GuiString label;

    private List<String> tooltip;

    public GuiIntegrityBar(int x, int y) {
        super(x, y, 0, 8);

        label = new GuiStringSmall(0, 0, "");
        label.setAttachment(GuiAttachment.topCenter);
        addChild(label);

        setAttachmentPoint(GuiAttachment.topCenter);

        tooltip = Collections.singletonList(I18n.format("tetra.stats.integrity_usage.tooltip"));
    }

    public void setItemStack(ItemStack itemStack, ItemStack previewStack) {
        boolean shouldShow = !itemStack.isEmpty() && itemStack.getItem() instanceof IModularItem;
        setVisible(shouldShow);
        if (shouldShow) {
            if (!previewStack.isEmpty()) {
                integrityGain = IModularItem.getIntegrityGain(previewStack);
                integrityCost = IModularItem.getIntegrityCost(previewStack);
            } else {
                integrityGain = IModularItem.getIntegrityGain(itemStack);
                integrityCost = IModularItem.getIntegrityCost(itemStack);
            }

            if (integrityGain - integrityCost < 0) {
                label.setString(TextFormatting.RED + I18n.format("tetra.stats.integrity_usage", integrityCost, integrityGain));
            } else {
                label.setString(I18n.format("tetra.stats.integrity_usage", integrityCost, integrityGain));
            }

            if (integrityGain > 7) {
                segmentWidth = Math.max(64 / integrityGain - 1, 1);
            } else {
                segmentWidth = 8;
            }

            width = integrityGain * ( segmentWidth + 1);
        }
    }

    public void showAnimation() {
        if (isVisible()) {
            new KeyframeAnimation(100, this)
                    .withDelay(200)
                    .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateY(-3, 0, true))
                    .start();
        }
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return super.getTooltipLines();
    }

    @Override
    public void draw(MatrixStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        for (int i = 0; i < integrityCost; i++) {
            if (i < integrityGain) {
                drawSegment(matrixStack, refX + x + i * (segmentWidth + 1),refY + y + segmentOffset,  costColor,
                        opacity * getOpacity());
            } else {
                drawSegment(matrixStack, refX + x + i * (segmentWidth + 1),refY + y + segmentOffset, overuseColor,
                        opacity * getOpacity());
            }
        }

        for (int i = integrityCost; i < integrityGain; i++) {
            drawSegment(matrixStack, refX + x + i * (segmentWidth + 1),refY + y + segmentOffset, gainColor,
                    opacity * getOpacity());
        }
    }

    private void drawSegment(MatrixStack matrixStack, int x, int y, int color, float opacity) {
        drawRect(matrixStack, x, y,x + segmentWidth, y + segmentHeight, color, opacity);
    }

}
