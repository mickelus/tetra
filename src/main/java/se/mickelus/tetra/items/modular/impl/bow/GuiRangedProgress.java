package se.mickelus.tetra.items.modular.impl.bow;

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

public class GuiRangedProgress extends GuiRoot {

    private static final int width = 16;

    private final GuiElement container;

    private final GuiRect bar;
    private final GuiRect overbowBar;

    private final KeyframeAnimation showAnimation;
    private final KeyframeAnimation hideAnimation;

    private float progress;

    public GuiRangedProgress(Minecraft mc) {
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

        overbowBar = new GuiRect(0, 2, 0, 1, GuiColors.warning);
        overbowBar.setAttachment(GuiAttachment.topRight);
        container.addChild(overbowBar);

        showAnimation = new KeyframeAnimation(60, container)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateX(0));

        hideAnimation = new KeyframeAnimation(100, container)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateX(-2))
                .withDelay(1000);
    }

    public void setProgress(float progress, float overbowProgress) {
        if (progress > 0 || overbowProgress > 0) {
            overbowBar.setWidth((int) (width * overbowProgress));
        }

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

        this.progress = progress;
    }

    @Override
    public void draw() {
        if (isVisible() && (progress > 0 || hideAnimation.isActive())) {
            MainWindow window = mc.getMainWindow();
            int width = window.getScaledWidth();
            int height = window.getScaledHeight();

            int mouseX = (int)(mc.mouseHelper.getMouseX() * window.getScaledWidth() / window.getWidth());
            int mouseY = (int)(mc.mouseHelper.getMouseY() * window.getScaledHeight() / window.getHeight());

            this.drawChildren(new MatrixStack(), width / 2, height / 2, 0, 0, mouseX, mouseY, 1.0F);
        }
    }
}
