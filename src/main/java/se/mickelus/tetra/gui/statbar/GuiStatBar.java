package se.mickelus.tetra.gui.statbar;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.util.CastOptional;

import java.util.List;

public class GuiStatBar extends GuiStatBase {

    protected static final int defaultBarMaxLength = 60;
    protected int barMaxLength = 60;
    protected final int barHeight = 1;

    protected static final String increaseColorFont = ChatFormatting.GREEN.toString();
    protected static final String decreaseColorFont = ChatFormatting.RED.toString();

    protected static final int increaseColorBar = 0x8855ff55;
    protected static final int decreaseColorBar = 0x88ff5555;
    protected int diffColor;

    protected double min;
    protected double max;

    protected GuiString labelString;
    protected GuiString valueString;
    protected GuiBar bar;

    protected List<String> tooltip;

    protected GuiAlignment alignment = GuiAlignment.left;


    public GuiStatBar(int x, int y, int barLength, String label, double min, double max, boolean segmented) {
        super(x, y, barLength, 16);


        setAttachmentAnchor(GuiAttachment.bottomCenter);

        barMaxLength = barLength;
        this.min = min;
        this.max = max;

        labelString = new GuiStringSmall(0, 0, label);
        valueString = new GuiString(0, 3, label);

        if (segmented) {
            bar = new GuiBarSegmented(height - 1, 0, barLength, min, max);
        } else {
            bar = new GuiBar(height - 1, 0, barLength, min, max);
        }

        addChild(labelString);
        addChild(valueString);
        addChild(bar);
    }

    public void setAlignment(GuiAlignment alignment) {
        this.alignment = alignment;
        realign();
    }

    private void realign() {
        if (alignment == GuiAlignment.right) {
            valueString.setX(-4);
        } else {
            valueString.setX(4);
        }

        labelString.setAttachment(alignment.toAttachment());
        valueString.setAttachmentPoint(alignment.toAttachment());
        valueString.setAttachmentAnchor(alignment.toAttachment().flipHorizontal());
    }

    @Override
    public void update(ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {

    }

    @Override
    public boolean shouldShow(ItemStack currentStack, ItemStack previewStack, String slot, String improvement) {
        return getValue(currentStack, null, null) > 0 || getPreviewValue(currentStack, previewStack, slot, improvement) > 0;
    }


    protected double getValue(ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> {
                    if (slot != null) {
                        return CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class)
                                .map(module -> {
                                    if (improvement != null) {
                                        item.getM
                                    } else {

                                    }
                                })

                    }
                })
                .orElse(-1);
    }

    protected double getPreviewValue(ItemStack itemStack, ItemStack previewStack, String slot, String improvement) {

    }

    protected String getLabel(double value, double previewValue) {

    }

    protected String getTooltip(double value) {

    }

    public void setValue(double value, double diffValue) {
        bar.setValue(value, diffValue);
        updateValueLabel(value, diffValue);
    }

    private void updateValueLabel(double value, double diffValue) {
        if (value != diffValue) {
            if (alignment == GuiAlignment.right) {
                valueString.setString(String.format("%s(%+.02f) %s%.02f",
                    value < diffValue ? increaseColorFont : decreaseColorFont, diffValue - value, ChatFormatting.RESET, diffValue));
            } else {
                valueString.setString(String.format("%.02f %s(%+.02f)",
                    diffValue, value < diffValue ? increaseColorFont : decreaseColorFont, diffValue - value));
            }
        } else {
            valueString.setString(String.format("%.02f", diffValue));
        }
    }
}
