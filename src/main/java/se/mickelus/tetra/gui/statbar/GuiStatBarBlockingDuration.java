package se.mickelus.tetra.gui.statbar;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.mgui.gui.GuiAlignment;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.gui.statbar.getter.*;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.ItemEffect;

public class GuiStatBarBlockingDuration extends GuiStatBar {
    private static final IStatGetter durationGetter = new StatGetterEffectLevel(ItemEffect.blocking, 1);

    private IStatGetter reflectGetter = new StatGetterEffectLevel(ItemEffect.blockingReflect, 1);
    private GuiTexture reflectIndicator;

    public GuiStatBarBlockingDuration(int x, int y, int width) {
        super(x, y, width, I18n.format("tetra.stats.blocking"), 0, ItemModularHandheld.blockingDurationLimit,
                false, durationGetter, LabelGetterBasic.integerLabel,
                new TooltipGetterBlockingDuration(durationGetter));

        reflectIndicator = new GuiTexture(labelString.getWidth() + 2, -1, 7, 7, 220, 0, GuiTextures.workbench);
        addChild(reflectIndicator);
    }

    @Override
    public void update(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        super.update(player, currentStack, previewStack, slot, improvement);

        if (durationGetter.getValue(player, currentStack) >= ItemModularHandheld.blockingDurationLimit
                || durationGetter.getValue(player, previewStack) >= ItemModularHandheld.blockingDurationLimit) {
            valueString.setString("");
        }

        updateIndicators(player, currentStack, previewStack, slot, improvement);
    }

    private void updateIndicators(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        int currentReflect = (int) reflectGetter.getValue(player, currentStack);
        int previewReflect = currentReflect;

        if (!previewStack.isEmpty()) {
            previewReflect = (int) reflectGetter.getValue(player, previewStack);
        } else if (slot != null) {
            if (improvement != null) {
                previewReflect = (int) reflectGetter.getValue(player, currentStack, slot, improvement);
                currentReflect -= previewReflect;
            } else {
                previewReflect = (int) reflectGetter.getValue(player, currentStack, slot);
                currentReflect -= previewReflect;
            }
        }

        reflectIndicator.setVisible(currentReflect > 0 || previewReflect > 0);
        reflectIndicator.setColor(getDiffColor(currentReflect, previewReflect));
    }

    @Override
    protected void realign() {
        super.realign();
        reflectIndicator.setAttachment(alignment.toAttachment().flipHorizontal());

        int offset = valueString.getWidth() + 2;
        reflectIndicator.setX(GuiAlignment.right.equals(alignment) ? offset : -offset);
    }

    protected int getDiffColor(int currentValue, int previewValue) {
        if (previewValue > currentValue) {
            return GuiColors.positive;
        } else if (previewValue < currentValue) {
            return GuiColors.negative;
        }

        return GuiColors.normal;
    }
}
