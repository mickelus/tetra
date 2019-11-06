package se.mickelus.tetra.gui.statbar;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.mgui.gui.GuiAlignment;
import se.mickelus.tetra.blocks.workbench.gui.GuiCapability;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.statbar.getter.*;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.util.CastOptional;

public class GuiStatBarCapability extends GuiStatBar {

    private static final int efficiencyMax = 50;

    private GuiCapability capabilityElement;
    private IStatGetter levelGetter;

    public GuiStatBarCapability(int x, int y, int width, Capability capability) {
        super(x, y, width, "", 0, efficiencyMax,
                false, new StatGetterCapabilityEfficiency(capability), LabelGetterBasic.decimalLabel,
                new TooltipGetterCapability(capability));

        bar.setWidth(width - 16);
        bar.setX(16);

        levelGetter = new StatGetterCapabilityLevel(capability);
        capabilityElement = new GuiCapability(-3, -3, capability);
        addChild(capabilityElement);
    }

    @Override
    public void update(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        super.update(player, currentStack, previewStack, slot, improvement);

        int level = (int) levelGetter.getValue(player, currentStack);
        int color = GuiColors.normal;

        if (!previewStack.isEmpty()) {
            int previewLevel = (int) levelGetter.getValue(player, previewStack);

            color = getDiffColor(level, previewLevel);
            level = previewLevel;
        } else if (slot != null) {
            int previewLevel = level - getSlotLevel(player, currentStack, slot, improvement);;

            color = getDiffColor(previewLevel, level);
        }

        capabilityElement.update(level, color);
    }

    @Override
    protected void realign() {
        super.realign();

        if (GuiAlignment.left.equals(alignment)) {
            bar.setX(16);
            capabilityElement.setX(-3);
        } else {
            bar.setX(0);
            capabilityElement.setX(0);
        }

        capabilityElement.setAttachment(alignment.toAttachment());
    }

    @Override
    public boolean shouldShow(PlayerEntity player, ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        return levelGetter.getValue(player, currentStack) > 0 || levelGetter.getValue(player, previewStack) > 0;
    }

    protected int getDiffColor(int currentValue, int previewValue) {
        if (previewValue > currentValue) {
            return GuiColors.positive;
        } else if (previewValue < currentValue) {
            return GuiColors.negative;
        }

        return GuiColors.normal;
    }

    protected int getSlotLevel(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> {
                    if (improvement != null) {
                        return levelGetter.getValue(player, itemStack, slot, improvement);
                    }

                    return levelGetter.getValue(player, itemStack, slot);
                })
                .orElse(-1d).intValue();
    }
}
