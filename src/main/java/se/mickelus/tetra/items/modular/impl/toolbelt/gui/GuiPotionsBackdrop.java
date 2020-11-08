package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiRect;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.impl.toolbelt.SlotType;
import se.mickelus.tetra.effect.ItemEffect;

import java.util.Collection;

public class GuiPotionsBackdrop extends GuiElement {
    public GuiPotionsBackdrop(int x, int y, int numSlots, Collection<Collection<ItemEffect>> inventoryEffects) {
        super(x, y, numSlots * 17 - 9, 28);

        setAttachmentPoint(GuiAttachment.topCenter);
        setAttachmentAnchor(GuiAttachment.topCenter);

        // background rects
        addChild(new GuiRect(0, 3, width, 22, 0xff000000));
        addChild(new GuiRect(0, 4, width, 1, GuiColors.mutedStrong));
        addChild(new GuiRect(0, 23, width, 1, GuiColors.muted));

        // left cap
        GuiTexture leftCap = new GuiTexture(0, 0, 16, 28, 64, 0, GuiTextures.toolbelt);
        leftCap.setAttachmentPoint(GuiAttachment.topRight);
        addChild(leftCap);

        GuiTexture rightCap = new GuiTexture(0, 0, 16, 28, 80, 0, GuiTextures.toolbelt);
        rightCap.setAttachmentPoint(GuiAttachment.topLeft);
        rightCap.setAttachmentAnchor(GuiAttachment.topRight);
        addChild(rightCap);

        GuiSlotEffect.getEffectsForInventory(SlotType.potion, inventoryEffects).forEach(this::addChild);
    }
}
