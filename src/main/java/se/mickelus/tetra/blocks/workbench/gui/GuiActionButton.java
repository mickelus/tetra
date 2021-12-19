package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import se.mickelus.mutil.gui.*;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.TetraToolActions;
import se.mickelus.tetra.blocks.workbench.action.WorkbenchAction;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class GuiActionButton extends GuiElement {

    private final WorkbenchAction action;
    private final ItemStack targetStack;

    private final ToolRequirementGui toolIndicator;

    private final GuiClickable iconClickable;
    private final GuiClickable labelClickable;

    private final GuiTexture borderLeft;
    private final GuiTexture borderRight;
    private final GuiRect borderTop;
    private final GuiRect borderBottom;

    public GuiActionButton(int x, int y, WorkbenchAction action, ItemStack targetStack, Consumer<WorkbenchAction> clickHandler) {
        this(x, y, action, targetStack, GuiAlignment.left, clickHandler);
    }

    public GuiActionButton(int x, int y, WorkbenchAction action, ItemStack targetStack, GuiAlignment alignment,
            Consumer<WorkbenchAction> clickHandler) {
        super(x, y, 0, 11);

        this.action = action;
        this.targetStack = targetStack;

        String label = I18n.get(String.format("%s.%s.label", TetraMod.MOD_ID, action.getKey()));
        width = Minecraft.getInstance().font.width(label) + 42;

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

        ToolAction requiredTool = action.getRequiredToolActions(targetStack).stream()
                .findFirst()
                .orElse(TetraToolActions.hammer);
        toolIndicator = new ToolRequirementGui(6, 7, requiredTool);
        iconClickable.addChild(toolIndicator);
    }

    private void setBorderColors(int color) {
        borderLeft.setColor(color);
        borderRight.setColor(color);
        borderTop.setColor(color);
        borderBottom.setColor(color);
    }

    public void update(Map<ToolAction, Integer> availableTools) {
        Map<ToolAction, Integer> requiredTools = action.getRequiredTools(targetStack);
        if (!requiredTools.isEmpty()) {
            toolIndicator.setTooltipVisibility(true);
            requiredTools.entrySet().stream()
                    .findFirst()
                    .ifPresent(entry -> toolIndicator.updateRequirement(entry.getValue(), availableTools.getOrDefault(entry.getKey(), 0)));
        } else {
            toolIndicator.setTooltipVisibility(false);
        }
    }
}
