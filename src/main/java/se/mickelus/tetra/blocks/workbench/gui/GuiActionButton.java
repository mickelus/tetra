package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.gui.*;

public class GuiActionButton extends GuiElement {

    private static final String WORKBENCH_TEXTURE = "textures/gui/workbench.png";

    private GuiCapabilityIndicator capabilityIndicator;

    private GuiClickable iconClickable;
    private GuiClickable labelClickable;

    private GuiTexture borderLeft;
    private GuiTexture borderRight;
    private GuiRect borderTop;
    private GuiRect borderBottom;

    public GuiActionButton(int x, int y, String label, Capability capability, Runnable onClickHandler) {
        super(x, y, 0, 11);

        width = Minecraft.getMinecraft().fontRenderer.getStringWidth(label) + 42;

        labelClickable = new GuiClickable(0, 0, width, height, onClickHandler) {
            protected void onFocus() {
                setBorderColors(0x8f8f6f);
            }

            protected void onBlur() {
                if (!iconClickable.hasFocus()) {
                    setBorderColors(0x7f7f7f);
                }
            }
        };

        labelClickable.addChild(new GuiRect(9, 0, width - 18, 11, 0));

        borderLeft = new GuiTexture(0, 0, 9, 11, 79, 0, WORKBENCH_TEXTURE).setColor(0x7f7f7f);
        labelClickable.addChild(borderLeft);
        borderRight = new GuiTexture(width - 9, 0, 9, 11, 88, 0, WORKBENCH_TEXTURE).setColor(0x7f7f7f);
        labelClickable.addChild(borderRight);

        borderTop = new GuiRect(9, 1, width - 18, 1, 0x7f7f7f);
        labelClickable.addChild(borderTop);
        borderBottom = new GuiRect(9, 9, width - 18, 1, 0x7f7f7f);
        labelClickable.addChild(borderBottom);

        labelClickable.addChild(new GuiStringOutline(35, 1, label));

        addChild(labelClickable);


        iconClickable = new GuiClickable(6, -9, 29, 29, onClickHandler) {
            protected void onFocus() {
                setBorderColors(0x8f8f6f);
            }

            protected void onBlur() {
                if (!labelClickable.hasFocus()) {
                    setBorderColors(0x7f7f7f);
                }
            }
        };

        iconClickable.addChild(new GuiTexture(0, 0, 29, 29, 52, 32, WORKBENCH_TEXTURE));
        addChild(iconClickable);

        capabilityIndicator = new GuiCapabilityIndicator(6, 7, capability);
        iconClickable.addChild(capabilityIndicator);
    }

    private void setBorderColors(int color) {
        borderLeft.setColor(color);
        borderRight.setColor(color);
        borderTop.setColor(color);
        borderBottom.setColor(color);
    }

    public void update(EntityPlayer entityPlayer, int requiredLevel) {
        capabilityIndicator.update(entityPlayer, requiredLevel);
    }
}
