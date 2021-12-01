package se.mickelus.tetra.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiStringSmall;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.Tooltips;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.stats.bar.GuiBar;
import se.mickelus.tetra.items.modular.IModularItem;

import java.util.Collections;
import java.util.List;

public class HoneProgressGui extends GuiElement {
    protected GuiString labelString;
    protected GuiString valueString;
    protected GuiBar bar;

    protected List<String> tooltip;
    protected List<String> extendedTooltip;

    public HoneProgressGui(int x, int y) {
        super(x, y, 45, 12);

        labelString = new GuiStringSmall(0, 0, I18n.get("item.tetra.modular.hone_progress.label"));
        addChild(labelString);

        valueString = new GuiStringSmall(0, 0, "");
        valueString.setAttachment(GuiAttachment.topRight);
        addChild(valueString);

        bar = new GuiBar(0, 0, width, 0, 1);
        addChild(bar);

    }

    public void update(ItemStack itemStack, boolean isPlaceholder) {
        boolean shouldShow = !isPlaceholder
                && itemStack.getItem() instanceof IModularItem
                && ConfigHandler.moduleProgression.get()
                && ((IModularItem) itemStack.getItem()).canGainHoneProgress();

        setVisible(shouldShow);
        if (shouldShow) {
            IModularItem item = (IModularItem) itemStack.getItem();
            int limit = item.getHoningLimit(itemStack);
            int progress = limit - item.getHoningProgress(itemStack);
            float factor = MathHelper.clamp(1f * progress / limit, 0, 1);
            float workableFactor = -item.getEffectLevel(itemStack, ItemEffect.workable);

            String factorString = String.format("%.0f%%", (100f * factor));

            String tooltipBase = I18n.get("item.tetra.modular.hone_progress.description",
                    progress, limit, factorString, item.getHoneBase(), item.getHoningIntegrityPenalty(itemStack));

            if (workableFactor < 0) {
                tooltipBase += I18n.get("item.tetra.modular.hone_progress.description_workable", String.format("%.0f%%", workableFactor));
            }

            tooltip = Collections.singletonList(tooltipBase + "\n \n" + Tooltips.expand.getString());
            extendedTooltip = Collections.singletonList(tooltipBase + "\n \n" + Tooltips.expanded.getString()
                    + "\n" + TextFormatting.GRAY.toString() + I18n.get("item.tetra.modular.hone_progress.description_extended"));

            valueString.setString(factorString);

            bar.setValue(factor, factor);

            if (factor < 1) {
                labelString.setColor(GuiColors.normal);
                valueString.setColor(GuiColors.normal);
                bar.setColor(GuiColors.normal);
            } else {
                labelString.setColor(GuiColors.hone);
                valueString.setColor(GuiColors.hone);
                bar.setColor(GuiColors.hone);
            }
        }
    }

    public void showAnimation() {
        if (isVisible()) {
            new KeyframeAnimation(100, this)
                    .withDelay(600)
                    .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateX(-3, 0, true))
                    .start();
        }
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            if (Screen.hasShiftDown()) {
                return extendedTooltip;
            }
            return tooltip;
        }
        return super.getTooltipLines();
    }
}
