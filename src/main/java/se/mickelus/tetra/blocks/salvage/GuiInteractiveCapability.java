package se.mickelus.tetra.blocks.salvage;

import net.minecraft.entity.player.EntityPlayer;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.gui.GuiTexture;
import se.mickelus.tetra.gui.animation.Applier;
import se.mickelus.tetra.gui.animation.KeyframeAnimation;

public class GuiInteractiveCapability extends GuiElement {
    private static final String texture = "textures/gui/block_interaction.png";

    private GuiTexture iconTexture;
    private GuiTexture levelTexture;

    private KeyframeAnimation show;
    private KeyframeAnimation hide;

    private Capability capability;
    private int capabilityLevel;
    private EntityPlayer player;

    public GuiInteractiveCapability(int x, int y, Capability capability, int capabilityLevel, EntityPlayer player) {
        super(x, y, 10, 10);
        opacity = 0;

        this.capability = capability;
        this.capabilityLevel = capabilityLevel;
        this.player = player;

        iconTexture = new GuiTexture(1, 0, 8, 8, capability.ordinal() * 8, 8, texture);
        addChild(iconTexture);

        levelTexture = new GuiTexture(2, 6, 5, 5, ( capabilityLevel - 1 ) * 5, 21, texture);
        addChild(levelTexture);

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
        int mainHandLevel = CapabilityHelper.getItemCapabilityLevel(player.getHeldItemMainhand(), capability);
        int offHandLevel = CapabilityHelper.getItemCapabilityLevel(player.getHeldItemOffhand(), capability);

        if (mainHandLevel >= capabilityLevel || offHandLevel >= capabilityLevel) {
            levelTexture.setColor(GuiColors.normal);
        } else if (CapabilityHelper.getPlayerCapabilityLevel(player, capability) >= capabilityLevel) {
            levelTexture.setColor(GuiColors.warning);
        } else {
            levelTexture.setColor(GuiColors.negative);
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
