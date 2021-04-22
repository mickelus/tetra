package se.mickelus.tetra.effect.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.effect.RevengeTracker;
import se.mickelus.tetra.gui.GuiTextures;

public class RevengeGui extends GuiElement {
    private GuiTexture indicatorLeft;

    private final KeyframeAnimation showAnimationLeft;
    private final KeyframeAnimation hideAnimationLeft;
    private final KeyframeAnimation showAnimationRight;
    private final KeyframeAnimation hideAnimationRight;

    public RevengeGui() {
        super(0, 14, 13, 3);
        setAttachment(GuiAttachment.middleCenter);

        indicatorLeft = new GuiTexture(-3, 0, 5, 3, 9, 4, GuiTextures.hud);
        addChild(indicatorLeft);

        GuiTexture indicatorRight = new GuiTexture(3, 0, 5, 3, 15, 4, GuiTextures.hud);
        indicatorRight.setAttachment(GuiAttachment.topRight);
        addChild(indicatorRight);

        showAnimationLeft = new KeyframeAnimation(120, indicatorLeft)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateX(0));

        hideAnimationLeft = new KeyframeAnimation(60, indicatorLeft)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateX(-3));

        showAnimationRight = new KeyframeAnimation(120, indicatorRight)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateX(0));

        hideAnimationRight = new KeyframeAnimation(60, indicatorRight)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateX(3));
    }

    public void update(PlayerEntity player, RayTraceResult mouseover) {
        if (mouseover.getType() == RayTraceResult.Type.ENTITY
                && RevengeTracker.canRevenge(player)
                && RevengeTracker.canRevenge(player, ((EntityRayTraceResult) mouseover).getEntity())) {
            if (!showAnimationLeft.isActive() && indicatorLeft.getOpacity() < 1) {
                showAnimationLeft.start();
                showAnimationRight.start();
            }
            hideAnimationLeft.stop();
            hideAnimationRight.stop();
        } else {
            if (!hideAnimationLeft.isActive() && indicatorLeft.getOpacity() > 0) {
                hideAnimationLeft.start();
                hideAnimationRight.start();
            }
            showAnimationLeft.stop();
            showAnimationRight.stop();
        }
    }
}
