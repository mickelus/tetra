package se.mickelus.tetra.effect.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiRoot;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.effect.ChargedAbilityEffect;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.InvertColorGui;

public class ComboPointGui extends GuiRoot {
    private GuiElement container;
    private Point[] points;

    private final KeyframeAnimation showAnimation;
    private final KeyframeAnimation hideAnimation;

    public ComboPointGui(Minecraft mc) {
        super(mc);

        container = new InvertColorGui(0, 18, 15, 3)
                .setAttachment(GuiAttachment.middleCenter)
                .setOpacity(0);
        addChild(container);

        points = new Point[4];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point(i * 4, 0);
            container.addChild(points[i]);
        }

        showAnimation = new KeyframeAnimation(60, container)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateY(18));

        hideAnimation = new KeyframeAnimation(100, container)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateY(15))
                .withDelay(200);
    }

    public void update(int points) {
        if (points > 0) {
            for (int i = 0; i < this.points.length; i++) {
                this.points[i].toggle(points > i);
            }

            if (!showAnimation.isActive() && container.getOpacity() < 1) {
                showAnimation.start();
            }
            hideAnimation.stop();
        } else {
            if (!hideAnimation.isActive() && container.getOpacity() > 0) {
                hideAnimation.start();
            }
            showAnimation.stop();
        }
    }

    public void draw(MatrixStack matrixStack) {
        if (isVisible()) {
            MainWindow window = mc.getMainWindow();
            int width = window.getScaledWidth();
            int height = window.getScaledHeight();

            int mouseX = (int)(mc.mouseHelper.getMouseX() * window.getScaledWidth() / window.getWidth());
            int mouseY = (int)(mc.mouseHelper.getMouseY() * window.getScaledHeight() / window.getHeight());

            this.drawChildren(matrixStack, width / 2, height / 2, 0, 0, mouseX, mouseY, 1.0F);
        }
    }

    static class Point extends GuiElement {
        private GuiTexture active;
        private GuiTexture inactive;

        public Point(int x, int y) {
            super(x, y, 3, 3);

            active = new GuiTexture(0, 0, 3, 3, 3, 4, GuiTextures.hud);
            addChild(active);

            inactive = new GuiTexture(0, 0, 3, 3, 6, 4, GuiTextures.hud);
            addChild(inactive);
        }

        public void toggle(boolean on) {
            active.setVisible(on);
            inactive.setVisible(!on);
        }
    }
}
