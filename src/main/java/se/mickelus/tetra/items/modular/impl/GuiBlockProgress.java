package se.mickelus.tetra.items.modular.impl;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiRect;
import se.mickelus.mutil.gui.GuiRoot;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
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
            bar.setWidth(Mth.clamp((int) (progress * width), 0, width));


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

    public void draw(PoseStack matrixStack) {
        if (isVisible()) {
            Window window = mc.getWindow();
            int width = window.getGuiScaledWidth();
            int height = window.getGuiScaledHeight();

            int mouseX = (int)(mc.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth());
            int mouseY = (int)(mc.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight());

            this.drawChildren(matrixStack, width / 2, height / 2, 0, 0, mouseX, mouseY, 1.0F);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }
}
