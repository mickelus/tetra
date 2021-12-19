package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import se.mickelus.mutil.gui.GuiAlignment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.blocks.workbench.action.WorkbenchAction;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class GuiActionList extends GuiElement {

    private GuiActionButton[] actionButtons;

    public GuiActionList(int x, int y) {
        super(x, y, 0, 0);

        actionButtons = new GuiActionButton[0];
    }

    public void updateActions(ItemStack targetStack, WorkbenchAction[] actions, Player player, Consumer<WorkbenchAction> clickHandler,
            WorkbenchTile tile) {
        WorkbenchAction[] availableActions = Arrays.stream(actions)
                .filter(action -> action.canPerformOn(player, tile, targetStack))
                .toArray(WorkbenchAction[]::new);

        actionButtons = new GuiActionButton[availableActions.length];

        clearChildren();
        int count = availableActions.length;
        setHeight(count * 2 + 20);
        for (int i = 0; i < count; i++) {
            GuiAlignment alignment = i % 2 == 0 ? GuiAlignment.left : GuiAlignment.right;
            actionButtons[i] = new GuiActionButton(count > 1 ? -9 : -20, i * 14, actions[i], targetStack, alignment, clickHandler);
            actionButtons[i].setAttachmentPoint(alignment.toAttachment());
            if (GuiAlignment.right.equals(alignment)) {
                actionButtons[i].setX(-actionButtons[i].getX());
            }
            addChild(actionButtons[i]);
        }
    }

    public void updateTools(Map<ToolAction, Integer> availableTools) {
        Arrays.stream(actionButtons).forEach(button -> button.update(availableTools));
    }

    public void showAnimation() {
        if (isVisible()) {
            for (int i = 0; i < getNumChildren(); i++) {
                new KeyframeAnimation(100, getChild(i))
                        .withDelay(i * 100 + 300)
                        .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateX(i % 2 == 0 ? -2 : 2, 0, true))
                        .start();
            }
        }
    }
}
