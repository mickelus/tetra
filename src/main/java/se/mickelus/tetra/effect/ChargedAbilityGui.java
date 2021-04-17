package se.mickelus.tetra.effect;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import se.mickelus.mgui.gui.*;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.InvertColorGui;

public class ChargedAbilityGui extends GuiRoot {
    private final int width = 17;
    
    private GuiElement container;
    private GuiElement overchargeContainer;
    private Bar bar;
    private Bar[] overchargeBars;

    private final KeyframeAnimation showAnimation;
    private final KeyframeAnimation hideAnimation;

    public ChargedAbilityGui(Minecraft mc) {
        super(mc);

        container = new InvertColorGui(-1, 20, width, 2)
                .setAttachment(GuiAttachment.middleCenter)
                .setOpacity(0);
        addChild(container);

        bar = new Bar(0, 0, width, 2);
        container.addChild(bar);


        overchargeContainer = new GuiElement(0, 3, width, 2);
        container.addChild(overchargeContainer);
        overchargeBars = new Bar[3];
        for (int i = 0; i < overchargeBars.length; i++) {
            overchargeBars[i] = new Bar(i * 6, 0, 5, 2);
            overchargeContainer.addChild(overchargeBars[i]);
        }

        showAnimation = new KeyframeAnimation(60, container)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateX(0));

        hideAnimation = new KeyframeAnimation(100, container)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateX(-2))
                .withDelay(1000);
    }

    public void setProgress(float progress, boolean canOvercharge) {
        if (progress > 0) {
            bar.setProgress(progress);

            overchargeContainer.setVisible(canOvercharge);
            if (canOvercharge) {
                double overchargeProgress = ChargedAbilityEffect.getOverchargeProgress(progress - 1);
                for (int i = 0; i < 3; i++) {
                    overchargeBars[i].setProgress(overchargeProgress - i);
                }
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

    static class Bar extends GuiElement {
        private GuiTexture bar;
        private GuiTexture background;

        public Bar(int x, int y, int width, int height) {
            super(x, y, width, height);

            bar = new GuiTexture(0, 0, 0, height, 3, 0, GuiTextures.hud);
            addChild(bar);

            background = new GuiTexture(0, 0, width, height, 3, 2, GuiTextures.hud);
            background.setAttachment(GuiAttachment.topRight);
            addChild(background);
        }

        public void setProgress(double progress) {
            int barWidth = MathHelper.clamp((int) (progress * width), 0, width);
            bar.setWidth(barWidth);
            background.setWidth(Math.max(0, width - barWidth));
        }
    }
}
