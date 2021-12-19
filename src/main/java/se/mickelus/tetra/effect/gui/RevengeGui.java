package se.mickelus.tetra.effect.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.effect.revenge.RevengeTracker;
import se.mickelus.tetra.gui.GuiTextures;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RevengeGui extends GuiElement {
    private final KeyframeAnimation showAnimationLeft;
    private final KeyframeAnimation hideAnimationLeft;
    private final KeyframeAnimation showAnimationRight;
    private final KeyframeAnimation hideAnimationRight;
    private final GuiTexture indicatorLeft;

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

    public void update(Player player, HitResult mouseover) {
        if (mouseover != null && mouseover.getType() == HitResult.Type.ENTITY
                && RevengeTracker.canRevenge(player)
                && RevengeTracker.canRevenge(player, ((EntityHitResult) mouseover).getEntity())) {
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
