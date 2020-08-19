package se.mickelus.tetra.blocks.salvage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.ToolType;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.mgui.gui.impl.GuiColors;
import se.mickelus.tetra.blocks.workbench.gui.GuiTool;
import se.mickelus.tetra.properties.PropertyHelper;

public class GuiInteractiveCapability extends GuiElement {
    private GuiTool toolIcon;

    private KeyframeAnimation show;
    private KeyframeAnimation hide;

    private ToolType toolType;
    private int capabilityLevel;
    private PlayerEntity player;

    public GuiInteractiveCapability(int x, int y, ToolType toolType, int capabilityLevel, PlayerEntity player) {
        super(x, y, 10, 10);
        opacity = 0;

        this.toolType = toolType;
        this.capabilityLevel = capabilityLevel;
        this.player = player;

        toolIcon = new GuiTool(1, 0, toolType);
        addChild(toolIcon);

        show = new KeyframeAnimation(100, this)
                .applyTo(new Applier.Opacity(0, 1))
                .withDelay(650);
        hide = new KeyframeAnimation(100, this)
                .applyTo(new Applier.Opacity(1, 0));

        updateTint();
    }

    public void updateFadeTime() {
        show = show.withDelay(0);
    }

    private void updateTint() {
        int mainHandLevel = PropertyHelper.getItemToolLevel(player.getHeldItemMainhand(), toolType);
        int offHandLevel = PropertyHelper.getItemToolLevel(player.getHeldItemOffhand(), toolType);

        if (mainHandLevel >= capabilityLevel || offHandLevel >= capabilityLevel) {
            toolIcon.update(capabilityLevel, GuiColors.normal);
        } else if (PropertyHelper.getPlayerToolLevel(player, toolType) >= capabilityLevel) {
            toolIcon.update(capabilityLevel, GuiColors.warning);
        } else {
            toolIcon.update(capabilityLevel, GuiColors.negative);
        }
    }

    public void show() {
        updateTint();
        hide.stop();
        show.start();
    }

    public void hide() {
        show.stop();
        hide.start();
    }
}
