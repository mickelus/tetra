package se.mickelus.tetra.blocks.salvage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.mgui.gui.impl.GuiColors;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.CapabilityHelper;

public class GuiInteractiveCapability extends GuiElement {
    private static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/block-interaction.png");

    private GuiTexture iconTexture;
    private GuiTexture levelTexture;

    private KeyframeAnimation show;
    private KeyframeAnimation hide;

    private Capability capability;
    private int capabilityLevel;
    private PlayerEntity player;

    public GuiInteractiveCapability(int x, int y, Capability capability, int capabilityLevel, PlayerEntity player) {
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
