package se.mickelus.tetra.effect.gui;

import net.minecraft.world.entity.player.Player;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.effect.ComboPoints;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.InvertColorGui;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class ComboPointGui extends GuiElement {
    private GuiElement container;
    private Point[] points;

    public ComboPointGui() {
        super(-1, 18, 15, 3);

        setAttachment(GuiAttachment.middleCenter);

        container = new InvertColorGui(0, 0, 15, 3);
        addChild(container);

        points = new Point[4];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point(i * 4, 0, i);
            container.addChild(points[i]);
        }
    }

    public void update(int points) {
        if (points > 0) {
            for (int i = 0; i < this.points.length; i++) {
                this.points[i].setVisible(true);
                this.points[i].toggle(points > i);
            }
        } else {
            for (int i = 0; i < this.points.length; i++) {
                this.points[i].setVisible(false);
            }
        }
    }

    public void update(Player player) {
        if (ComboPoints.canSpend(player)) {
            update(ComboPoints.get(player));
        } else {
            update(0);
        }
    }

    static class Point extends GuiElement {
        private GuiTexture active;
        private GuiTexture inactive;

        private final KeyframeAnimation showAnimation;
        private final KeyframeAnimation hideAnimation;

        public Point(int x, int y, int offset) {
            super(x, y - 3, 3, 3);

            active = new GuiTexture(0, 0, 3, 3, 3, 4, GuiTextures.hud);
            addChild(active);

            inactive = new GuiTexture(0, 0, 3, 3, 6, 4, GuiTextures.hud);
            addChild(inactive);


            showAnimation = new KeyframeAnimation(60, this)
                    .applyTo(new Applier.Opacity(1), new Applier.TranslateY(y))
                    .withDelay(50 * offset);

            hideAnimation = new KeyframeAnimation(100, this)
                    .applyTo(new Applier.Opacity(0), new Applier.TranslateY(y - 3))
                    .withDelay(50 * offset)
                    .onStop(complete -> { if (complete) isVisible = false; });
        }

        public void toggle(boolean on) {
            active.setVisible(on);
            inactive.setVisible(!on);
        }

        @Override
        protected void onShow() {
            if (!showAnimation.isActive()) {
                showAnimation.start();
            }
            hideAnimation.stop();
        }

        @Override
        protected boolean onHide() {
            if (!hideAnimation.isActive()) {
                hideAnimation.start();
            }
            showAnimation.stop();
            return false;
        }
    }
}
