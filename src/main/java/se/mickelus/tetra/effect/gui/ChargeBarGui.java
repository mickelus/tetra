package se.mickelus.tetra.effect.gui;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.effect.ChargedAbilityEffect;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.InvertColorGui;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
@ParametersAreNonnullByDefault
public class ChargeBarGui extends GuiElement {
    private GuiElement container;
    private GuiElement overchargeContainer;
    private Bar bar;
    private Bar[] overchargeBars;

    private final KeyframeAnimation showAnimation;
    private final KeyframeAnimation hideAnimation;

    public ChargeBarGui() {
        super(-1, 22, 17, 2);

        setAttachment(GuiAttachment.middleCenter);

        container = new InvertColorGui(0, 0, width, height)
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

    public void update(Player player) {

        ItemStack activeStack = player.getUseItem();
        ItemModularHandheld item = CastOptional.cast(activeStack.getItem(), ItemModularHandheld.class).orElse(null);
        ChargedAbilityEffect ability = Optional.ofNullable(item).map(i -> i.getChargeableAbility(activeStack)).orElse(null);

        if (ability != null) {
            setProgress(getProgress(player, item, activeStack, ability), ability.canOvercharge(item, activeStack));
        } else {
            setProgress(0, false);
        }
    }

    private float getProgress(Player player, ItemModularHandheld item, ItemStack itemStack, ChargedAbilityEffect ability) {
        return ability != null ? (itemStack.getUseDuration() - player.getUseItemRemainingTicks()) * 1f / ability.getChargeTime(player, item, itemStack) : 0;
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
            int barWidth = Mth.clamp((int) (progress * width), 0, width);
            bar.setWidth(barWidth);
            background.setWidth(Math.max(0, width - barWidth));
        }
    }
}
