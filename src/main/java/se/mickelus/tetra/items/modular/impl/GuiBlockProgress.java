package se.mickelus.tetra.items.modular.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiRect;
import se.mickelus.mgui.gui.GuiRoot;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;

public class GuiBlockProgress extends GuiRoot {

    private static final int width = 16;

    private GuiElement container;

    private GuiRect bar;

    private final KeyframeAnimation showAnimation;
    private final KeyframeAnimation hideAnimation;

    public GuiBlockProgress(Minecraft mc) {
        super(mc);

        container = new GuiElement(-1, 20, 16, 2)
                .setAttachment(GuiAttachment.middleCenter)
                .setOpacity(0);
        addChild(container);

        container.addChild(new GuiRect(0, 0, 16, 2, GuiColors.normal)
                .setOpacity(0.2f));

        bar = new GuiRect(0, 0, 0, 2, GuiColors.normal);
        bar.setOpacity(0.6f);
        container.addChild(bar);

        showAnimation = new KeyframeAnimation(60, container)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateX(0));

        hideAnimation = new KeyframeAnimation(100, container)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateX(-2))
                .withDelay(1000);
    }

    public void setProgress(float progress) {
        if (progress > 0) {
            bar.setWidth(MathHelper.clamp((int) (progress * width), 0, width));


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
}
