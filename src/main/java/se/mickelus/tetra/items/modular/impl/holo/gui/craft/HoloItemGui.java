package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import se.mickelus.mgui.gui.*;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.GuiAnimation;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.IModularItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HoloItemGui extends GuiClickable {

    GuiElement slotGroup;

    private List<GuiAnimation> selectAnimations;
    private List<GuiAnimation> deselectAnimations;

    private List<GuiAnimation> hoverAnimations;
    private List<GuiAnimation> blurAnimations;

    private KeyframeAnimation itemShow;
    private KeyframeAnimation itemHide;

    private boolean isSelected = false;
    private final GuiTexture backdrop;

    private final GuiTexture icon;

    private IModularItem item;

    public HoloItemGui(int x, int y, IModularItem item, int textureIndex, Runnable onSelect, Consumer<String> onSlotSelect) {
        super(x, y, 64, 64, onSelect);

        selectAnimations = new ArrayList<>();
        deselectAnimations = new ArrayList<>();

        hoverAnimations = new ArrayList<>();
        blurAnimations = new ArrayList<>();

        backdrop = new GuiTexture(0, 0, 52, 52, GuiTextures.workbench);
        backdrop.setAttachment(GuiAttachment.middleCenter);
        addChild(backdrop);

        icon = new GuiTexture(0, 0, 38, 38, 38 * (textureIndex % 6), 218 - 38 * (textureIndex / 6), GuiTextures.workbench);
        icon.setAttachment(GuiAttachment.middleCenter);
        addChild(icon);

        GuiElement labelGroup = new GuiElement(0, 0, 0, 0);
        String[] labelStrings = I18n.format("tetra.holo.craft." + item.getItem().getRegistryName().getPath()).split(" ");

        for (int i = 0; i < labelStrings.length; i++) {
            GuiString labelLine = new GuiStringOutline(0, i * 10, labelStrings[i]);
            labelLine.setAttachment(GuiAttachment.topCenter);
            labelLine.setColor(GuiColors.hover);

            labelGroup.addChild(labelLine);
        }

        labelGroup.setAttachment(GuiAttachment.middleCenter);
        labelGroup.setHeight(10 * labelStrings.length);
        labelGroup.setOpacity(0);
        addChild(labelGroup);

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

        // hover/blur animations
        hoverAnimations.add(new KeyframeAnimation(80, labelGroup)
                .applyTo(new Applier.Opacity(1), new Applier.TranslateY(-2, 0)));

        blurAnimations.add(new KeyframeAnimation(120, labelGroup)
                .applyTo(new Applier.Opacity(0), new Applier.TranslateY(0, 2)));

        this.item = item;
    }

    @Override
    public boolean onMouseClick(int x, int y, int button) {
        for (int i = elements.size() - 1; i >= 0; i--) {
            if (elements.get(i).isVisible()) {
                if (elements.get(i).onMouseClick(x, y, button)) {
                    return true;
                }
            }
        }

        return super.onMouseClick(x, y, button);
    }

    public void onItemSelected(IModularItem item) {
        if (this.item.equals(item)) {
            setVisible(true);
            setSelected(true);
        } else if (item == null) {
            setVisible(true);
            setSelected(false);
        } else {
            setVisible(false);
            setSelected(false);
        }
    }

    public void setSelected(boolean selected) {
        if (selected) {
            deselectAnimations.forEach(GuiAnimation::stop);

            slotGroup.setVisible(true);
            selectAnimations.forEach(GuiAnimation::start);

            icon.setColor(GuiColors.normal);
            hoverAnimations.forEach(GuiAnimation::stop);
            blurAnimations.forEach(GuiAnimation::start);
        } else {
            selectAnimations.forEach(GuiAnimation::stop);
            deselectAnimations.forEach(GuiAnimation::start);
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

            icon.setColor(GuiColors.muted);
            blurAnimations.forEach(GuiAnimation::stop);
            hoverAnimations.forEach(GuiAnimation::start);
        }
    }

    @Override
    protected void onBlur() {
        backdrop.setColor(GuiColors.normal);

        icon.setColor(GuiColors.normal);
        hoverAnimations.forEach(GuiAnimation::stop);
        blurAnimations.forEach(GuiAnimation::start);
    }

    private void setupSlots(IModularItem item, Consumer<String> onSlotSelect) {
        String[] majorModuleNames = item.getMajorModuleNames();
        String[] majorModuleKeys = item.getMajorModuleKeys();
        GuiModuleOffsets majorOffsets = item.getMajorGuiOffsets();

        String[] minorModuleNames = item.getMinorModuleNames();
        String[] minorModuleKeys = item.getMinorModuleKeys();
        GuiModuleOffsets minorOffsets = item.getMinorGuiOffsets();

        for (int i = 0; i < majorModuleNames.length; i++) {
            final int x = majorOffsets.getX(i);
            GuiAttachment attachment = x > 0 ? GuiAttachment.topLeft : GuiAttachment.topRight;
            slotGroup.addChild(new HoloSlotMajorGui(x, majorOffsets.getY(i), attachment,
                    majorModuleKeys[i], majorModuleNames[i], onSlotSelect));
        }

        for (int i = 0; i < minorModuleNames.length; i++) {
            final int x = minorOffsets.getX(i);
            GuiAttachment attachment = x > 0 ? GuiAttachment.topLeft : GuiAttachment.topRight;
            slotGroup.addChild(new HoloSlotGui(x, minorOffsets.getY(i), attachment,
                    minorModuleKeys[i], minorModuleNames[i], onSlotSelect));
        }
    }
}
