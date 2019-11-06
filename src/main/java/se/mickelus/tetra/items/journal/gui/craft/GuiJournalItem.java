package se.mickelus.tetra.items.journal.gui.craft;

import se.mickelus.mgui.gui.*;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.ItemModular;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GuiJournalItem extends GuiClickable {

    GuiElement slotGroup;

    private List<KeyframeAnimation> selectAnimations;
    private List<KeyframeAnimation> deselectAnimations;

    private KeyframeAnimation itemShow;
    private KeyframeAnimation itemHide;

    private boolean isSelected = false;
    private final GuiTexture backdrop;

    public GuiJournalItem(int x, int y, ItemModular item, String label, Runnable onSelect, Consumer<String> onSlotSelect) {
        super(x, y, 64, 64, onSelect);

        selectAnimations = new ArrayList<>();
        deselectAnimations = new ArrayList<>();

        backdrop = new GuiTexture(0, 0, 52, 52, GuiTextures.workbench);
        backdrop.setAttachment(GuiAttachment.middleCenter);
        addChild(backdrop);

        GuiString labelString = new GuiStringOutline(0, -1, label);
        labelString.setAttachment(GuiAttachment.middleCenter);
        addChild(labelString);

        // label animations
        selectAnimations.add(new KeyframeAnimation(80, labelString)
                .applyTo(new Applier.Opacity(0)));
        deselectAnimations.add(new KeyframeAnimation(80, labelString)
                .applyTo(new Applier.Opacity(1)));

        slotGroup = new GuiElement(37, 15, 0, 0);
        setupSlots(item, onSlotSelect);
        slotGroup.setVisible(false);
        addChild(slotGroup);

        // slot animations
        selectAnimations.add(new KeyframeAnimation(80, slotGroup)
                .applyTo(new Applier.Opacity(1)));
        deselectAnimations.add(new KeyframeAnimation(80, slotGroup)
                .applyTo(new Applier.Opacity(0))
                .onStop(complete -> {
                    if (complete) slotGroup.setVisible(false);
                }));

        // item animations
        selectAnimations.add(new KeyframeAnimation(80, this)
                .applyTo(new Applier.TranslateX(0), new Applier.TranslateY(0)));
        deselectAnimations.add(new KeyframeAnimation(80, this)
                .applyTo(new Applier.TranslateX(x), new Applier.TranslateY(y)));

        itemShow = new KeyframeAnimation(80, this)
                .applyTo(new Applier.Opacity(1));

        itemHide = new KeyframeAnimation(80, this)
                .applyTo(new Applier.Opacity(0))
                .onStop(complete -> this.isVisible = false);
    }

    @Override
    public boolean onClick(int x, int y) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            if (elements.get(i).isVisible()) {
                if (elements.get(i).onClick(x, y)) {
                    return true;
                }
            }
        }

        return super.onClick(x, y);
    }

    public void setSelected(boolean selected) {
        if (selected) {
            deselectAnimations.forEach(KeyframeAnimation::stop);

            slotGroup.setVisible(true);
            selectAnimations.forEach(KeyframeAnimation::start);
        } else {
            selectAnimations.forEach(KeyframeAnimation::stop);
            deselectAnimations.forEach(KeyframeAnimation::start);
        }

        backdrop.setColor(GuiColors.normal);
        isSelected = selected;
    }

    @Override
    protected void onShow() {
        super.onShow();
        itemHide.stop();
        itemShow.start();
    }

    @Override
    protected boolean onHide() {
        super.onHide();
        itemShow.stop();
        itemHide.start();

        return false;
    }

    @Override
    protected void calculateFocusState(int refX, int refY, int mouseX, int mouseY) {
        mouseX -= refX + x;
        mouseY -= refY + y;
        boolean gainFocus = true;

        if (mouseX + mouseY < 44) {
            gainFocus = false;
        }

        if (mouseX + mouseY > 84) {
            gainFocus = false;
        }

        if (mouseX - mouseY > 16) {
            gainFocus = false;
        }

        if (mouseY - mouseX > 19) {
            gainFocus = false;
        }

        if (gainFocus != hasFocus) {
            hasFocus = gainFocus;
            if (hasFocus) {
                onFocus();
            } else {
                onBlur();
            }
        }
    }

    @Override
    protected void onFocus() {
        if (!isSelected) {
            backdrop.setColor(GuiColors.hover);
        }
    }

    @Override
    protected void onBlur() {
        backdrop.setColor(GuiColors.normal);
    }

    private void setupSlots(ItemModular item, Consumer<String> onSlotSelect) {
        String[] majorModuleNames = item.getMajorModuleNames();
        String[] majorModuleKeys = item.getMajorModuleKeys();
        GuiModuleOffsets majorOffsets = GuiModuleOffsets.getMajorOffsets(item);

        String[] minorModuleNames = item.getMinorModuleNames();
        String[] minorModuleKeys = item.getMinorModuleKeys();
        GuiModuleOffsets minorOffsets = GuiModuleOffsets.getMinorOffsets(item);

        for (int i = 0; i < majorModuleNames.length; i++) {
            final int x = majorOffsets.getX(i);
            GuiAttachment attachment = x > 0 ? GuiAttachment.topLeft : GuiAttachment.topRight;
            slotGroup.addChild(new GuiJournalSlotMajor(x, majorOffsets.getY(i), attachment,
                    majorModuleKeys[i], majorModuleNames[i], onSlotSelect));
        }

        for (int i = 0; i < minorModuleNames.length; i++) {
            final int x = minorOffsets.getX(i);
            GuiAttachment attachment = x > 0 ? GuiAttachment.topLeft : GuiAttachment.topRight;
            slotGroup.addChild(new GuiJournalSlot(x, minorOffsets.getY(i), attachment,
                    minorModuleKeys[i], minorModuleNames[i], onSlotSelect));
        }
    }
}
