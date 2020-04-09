package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import se.mickelus.mgui.gui.*;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.blocks.workbench.action.WorkbenchAction;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

import java.util.function.Consumer;

public class GuiActionButton extends GuiElement {

    private WorkbenchAction action;
    private ItemStack targetStack;

    private GuiCapabilityRequirement capabilityIndicator;

    private GuiClickable iconClickable;
    private GuiClickable labelClickable;

    private GuiTexture borderLeft;
    private GuiTexture borderRight;
    private GuiRect borderTop;
    private GuiRect borderBottom;

    public GuiActionButton(int x, int y, WorkbenchAction action, ItemStack targetStack, Consumer<WorkbenchAction> clickHandler) {
        this(x, y, action, targetStack, GuiAlignment.left, clickHandler);
    }

    public GuiActionButton(int x, int y, WorkbenchAction action, ItemStack targetStack, GuiAlignment alignment,
            Consumer<WorkbenchAction> clickHandler) {
        super(x, y, 0, 11);

        this.action = action;
        this.targetStack = targetStack;

        String label = I18n.format(String.format("%s.%s.label", TetraMod.MOD_ID, action.getKey()));
        width = Minecraft.getInstance().fontRenderer.getStringWidth(label) + 42;

        labelClickable = new GuiClickable(0, 0, width, height, () -> clickHandler.accept(action)) {
            protected void onFocus() {
                setBorderColors(GuiColors.hoverMuted);
            }

            protected void onBlur() {
                if (!iconClickable.hasFocus()) {
                    setBorderColors(GuiColors.muted);
                }
            }
        };

        labelClickable.addChild(new GuiRect(9, 0, width - 18, 11, 0));

        borderLeft = new GuiTexture(0, 0, 9, 11, 79, 0, GuiTextures.workbench).setColor(GuiColors.muted);
        labelClickable.addChild(borderLeft);
        borderRight = new GuiTexture(width - 9, 0, 9, 11, 88, 0, GuiTextures.workbench).setColor(GuiColors.muted);
        labelClickable.addChild(borderRight);

        borderTop = new GuiRect(9, 1, width - 18, 1, GuiColors.muted);
        labelClickable.addChild(borderTop);
        borderBottom = new GuiRect(9, 9, width - 18, 1, GuiColors.muted);
        labelClickable.addChild(borderBottom);

        GuiString labelString = new GuiStringOutline(7, 1, label);
        labelString.setAttachment(alignment.flip().toAttachment());
        if (GuiAlignment.left.equals(alignment)) {
            labelString.setX(-labelString.getX());
        }
        labelClickable.addChild(labelString);

        addChild(labelClickable);

        iconClickable = new GuiClickable(6, -9, 29, 29, () -> clickHandler.accept(action)) {
            protected void onFocus() {
                setBorderColors(GuiColors.hoverMuted);
            }

            protected void onBlur() {
                if (!labelClickable.hasFocus()) {
                    setBorderColors(GuiColors.muted);
                }
            }
        };
        iconClickable.setAttachment(alignment.toAttachment());
        if (GuiAlignment.right.equals(alignment)) {
            iconClickable.setX(-iconClickable.getX());
        }

        iconClickable.addChild(new GuiTexture(0, 0, 29, 29, 97, 0, GuiTextures.workbench));
        addChild(iconClickable);

        Capability[] capabilities = action.getRequiredCapabilitiesFor(targetStack);
        capabilityIndicator = new GuiCapabilityRequirement(6, 7, capabilities.length > 0 ? capabilities[0] : Capability.hammer);
        iconClickable.addChild(capabilityIndicator);
    }

    private void setBorderColors(int color) {
        borderLeft.setColor(color);
        borderRight.setColor(color);
        borderTop.setColor(color);
        borderBottom.setColor(color);
    }

    public void update(int[] availableCapabilities) {
        Capability[] capabilities = action.getRequiredCapabilitiesFor(targetStack);

        if (capabilities.length > 0) {
            capabilityIndicator.updateRequirement(action.getCapabilityLevel(targetStack, capabilities[0]),
                    availableCapabilities[capabilities[0].ordinal()]);

        }
    }
}
