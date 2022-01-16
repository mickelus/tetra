package se.mickelus.tetra.blocks.workbench.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.mutil.gui.GuiStringSmall;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.items.modular.IModularItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
public class GuiIntegrityBar extends GuiElement {

    private static final int segmentHeight = 2;
    private static final int segmentOffset = 6;
    private static final int gainColor = 0x22ffffff;
    private static final float gainOpacity = 0.15f;
    private static final int costColor = 0xffffffff;
    private static final int overuseColor = 0x88ff5555;
    private static final float overuseOpacity = 0.55f;
    private final GuiString label;
    private final List<Component> tooltip;
    private int segmentWidth = 8;
    private int integrityGain;
    private int integrityCost;

    public GuiIntegrityBar(int x, int y) {
        super(x, y, 0, 8);

        label = new GuiStringSmall(0, 0, "");
        label.setAttachment(GuiAttachment.topCenter);
        addChild(label);

        setAttachmentPoint(GuiAttachment.topCenter);

        tooltip = Collections.singletonList(new TranslatableComponent("tetra.stats.integrity_usage.tooltip"));
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
                label.setString(ChatFormatting.RED + I18n.get("tetra.stats.integrity_usage", integrityCost, integrityGain));
            } else {
                label.setString(I18n.get("tetra.stats.integrity_usage", integrityCost, integrityGain));
            }

            if (integrityGain > 7) {
                segmentWidth = Math.max(64 / integrityGain - 1, 1);
            } else {
                segmentWidth = 8;
            }

            width = integrityGain * (segmentWidth + 1);
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
    public List<Component> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return super.getTooltipLines();
    }

    @Override
    public void draw(PoseStack matrixStack, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(matrixStack, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        for (int i = 0; i < integrityCost; i++) {
            if (i < integrityGain) {
                drawSegment(matrixStack, refX + x + i * (segmentWidth + 1), refY + y + segmentOffset, costColor,
                        opacity * getOpacity());
            } else {
                drawSegment(matrixStack, refX + x + i * (segmentWidth + 1), refY + y + segmentOffset, overuseColor,
                        opacity * getOpacity());
            }
        }

        for (int i = integrityCost; i < integrityGain; i++) {
            drawSegment(matrixStack, refX + x + i * (segmentWidth + 1), refY + y + segmentOffset, gainColor,
                    opacity * getOpacity());
        }
    }

    private void drawSegment(PoseStack matrixStack, int x, int y, int color, float opacity) {
        drawRect(matrixStack, x, y, x + segmentWidth, y + segmentHeight, color, opacity);
    }

}
