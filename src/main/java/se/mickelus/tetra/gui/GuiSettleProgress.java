package se.mickelus.tetra.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiStringSmall;
import se.mickelus.tetra.gui.statbar.GuiBar;
import se.mickelus.tetra.module.ItemModuleMajor;

import java.util.Collections;
import java.util.List;

public class GuiSettleProgress extends GuiElement {
    protected GuiString labelString;
    protected GuiString valueString;
    protected GuiBar bar;

    protected List<String> tooltip;

    public GuiSettleProgress(int x, int y, int barLength) {
        super(x, y, barLength, 12);

        labelString = new GuiStringSmall(0, 0, I18n.format("item.modular.settle_progress.label"));
        addChild(labelString);

        valueString = new GuiStringSmall(0, 0, "");
        valueString.setAttachment(GuiAttachment.topRight);
        addChild(valueString);

        bar = new GuiBar(0, 0, barLength, 0, 1);
        addChild(bar);

    }

    public void update(ItemStack itemStack, ItemModuleMajor module) {
        int value = module.getSettleProgress(itemStack);
        int limit = module.getSettleLimit(itemStack);
        float progress = (1f * limit - value) / limit;

        int settleMaxCount = module.getSettleMaxCount(itemStack);

        boolean fullySettled = settleMaxCount <= module.getImprovementLevel(itemStack, ItemModuleMajor.settleImprovement);
        boolean isArrested = module.getImprovementLevel(itemStack, ItemModuleMajor.arrestedImprovement) != -1;
        boolean isGain = module.getIntegrityGain(itemStack) > 0;

        if (isArrested) {
            labelString.setString(TextFormatting.RED + I18n.format("arrested.name"));
            labelString.setAttachment(GuiAttachment.topCenter);
            tooltip = Collections.singletonList(I18n.format("arrested.description"));

            valueString.setString("");
            bar.setValue(0, 0);
        } else if (fullySettled) {
            labelString.setString(TextFormatting.GREEN + I18n.format("item.modular.settle_full.label"));
            labelString.setAttachment(GuiAttachment.topCenter);

            if (isGain) {
                tooltip = Collections.singletonList(I18n.format("item.modular.settle_full_gain.description"));
            } else {
                tooltip = Collections.singletonList(I18n.format("item.modular.settle_full_cost.description"));
            }

            valueString.setString("");
            bar.setValue(1f, 1f);
        } else if (settleMaxCount == 0) {
            labelString.setString(I18n.format("item.modular.settle_full_null.label"));
            labelString.setAttachment(GuiAttachment.topCenter);
            tooltip = Collections.singletonList(I18n.format("item.modular.settle_full_null.description"));

            valueString.setString("");
            bar.setValue(1f, 1f);
        } else {
            float durabilityPenalty = Math.max(module.getImprovementLevel(itemStack, ItemModuleMajor.settleImprovement) * ConfigHandler.settleLimitLevelMultiplier, 1f)
                    * module.getDurability(itemStack) * ConfigHandler.settleLimitDurabilityMultiplier;

            labelString.setString(I18n.format("item.modular.settle_progress.label"));
            labelString.setAttachment(GuiAttachment.topLeft);
            tooltip = Collections.singletonList(I18n.format(isGain ? "item.modular.settle_progress_gain.description" : "item.modular.settle_progress_cost.description",
                    limit - value /*String.format("%.0f", (100f * progress))*/, limit, ConfigHandler.settleLimitBase, (int) durabilityPenalty));

            valueString.setString(String.format("%.0f%%", (100f * progress)));

            bar.setValue((1f * progress), (1f * progress));
        }
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            return tooltip;
        }
        return super.getTooltipLines();
    }
}
