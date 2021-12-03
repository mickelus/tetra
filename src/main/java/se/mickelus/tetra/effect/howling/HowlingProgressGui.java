package se.mickelus.tetra.effect.howling;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiRoot;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.InvertColorGui;

import javax.annotation.ParametersAreNonnullByDefault;
@ParametersAreNonnullByDefault
public class HowlingProgressGui extends GuiRoot {

    private static final int width = 16;

    private GuiElement backdrop;
    private GuiElement container;

    private HowlingIndicatorGui[] indicators;

    private final KeyframeAnimation showAnimation;
    private final KeyframeAnimation hideAnimation;

    public HowlingProgressGui(Minecraft mc) {
        super(mc);

        container = new GuiElement(-1, 40, 15, 15)
                .setAttachment(GuiAttachment.middleCenter)
                .setOpacity(0);
        addChild(container);


        GuiElement indicatorGroup = new InvertColorGui(0, 0);
        container.addChild(indicatorGroup);

//        backdrop = new GuiTexture(0, 0, 15, 15, 5, 0, GuiTextures.hud).setColor(GuiColors.muted);
//        container.addChild(backdrop);

        indicators = new HowlingIndicatorGui[12];
        indicators[0] = new HowlingIndicatorGui(11, 2, 3, 4, 21, 0, 2, false);
        indicators[1] = new HowlingIndicatorGui(7, 0, 3, 4, 21, 0, 2, false);
        indicators[2] = new HowlingIndicatorGui(2, 1, 4, 3, 21, 7, 2, true);
        indicators[3] = new HowlingIndicatorGui(0, 5, 4, 3, 21, 7, 2, true);
        indicators[4] = new HowlingIndicatorGui(1, 9, 3, 4, 24, 0, -2, false);
        indicators[5] = new HowlingIndicatorGui(5, 11, 3, 4, 24, 0, -2, false);
        indicators[6] = new HowlingIndicatorGui(9, 11, 4, 3, 21, 4, -2, true);
        indicators[7] = new HowlingIndicatorGui(11, 7, 4, 3, 21, 4, -2, true);

        indicators[8] = new HowlingIndicatorGui(9, 5, 3, 3, 22, 4, -2, true);
        indicators[9] = new HowlingIndicatorGui(5, 3, 3, 3, 21, 0, 2, false);
        indicators[10] = new HowlingIndicatorGui(3, 7, 3, 3, 21, 7, 2, true);
        indicators[11] = new HowlingIndicatorGui(7, 9, 3, 3, 24, 1, -2, false);

        for (HowlingIndicatorGui indicator : indicators) {
            indicatorGroup.addChild(indicator);
        }

        showAnimation = new KeyframeAnimation(60, container)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateY(40));

        hideAnimation = new KeyframeAnimation(100, container)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateY(42))
                .withDelay(500);
    }

    public void updateAmplifier(int progress) {
        if (progress > -1) {

            if (!showAnimation.isActive() && container.getOpacity() < 1) {
                showAnimation.start();
            }
            hideAnimation.stop();

            for (int i = 0; i < indicators.length; i++) {
                if (i <= progress) {
                    indicators[i].show();
                } else {
                    indicators[i].reset();
                }
            }
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

            int mouseX = (int) (mc.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth());
            int mouseY = (int) (mc.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight());

            this.drawChildren(matrixStack, width / 2, height / 2, 0, 0, mouseX, mouseY, 1.0F);
        }
    }
}
