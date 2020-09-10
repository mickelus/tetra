package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.mgui.gui.*;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.impl.toolbelt.SlotType;
import se.mickelus.tetra.module.ItemEffect;

import java.util.Collection;

public class GuiQuickSlotBackdrop extends GuiElement {
    public GuiQuickSlotBackdrop(int x, int y, int numSlots, Collection<Collection<ItemEffect>> inventoryEffects) {
        super(x, y, numSlots * 17 - 9, 28);

        setAttachmentPoint(GuiAttachment.topCenter);
        setAttachmentAnchor(GuiAttachment.topCenter);

        // background rects
        addChild(new GuiRect(0, 3, width, 22, 0xff000000));
        addChild(new GuiRect(0, 4, width, 20, GuiColors.muted));
        addChild(new GuiRect(0, 5, width, 18, 0xff000000));

        // left cap
        GuiTexture leftCap = new GuiTexture(0, 0, 16, 28, GuiTextures.toolbelt);
        leftCap.setAttachmentPoint(GuiAttachment.topRight);
        addChild(leftCap);

        GuiTexture rightCap = new GuiTexture(0, 0, 16, 28, 16, 0, GuiTextures.toolbelt);
        rightCap.setAttachmentPoint(GuiAttachment.topLeft);
        rightCap.setAttachmentAnchor(GuiAttachment.topRight);
        addChild(rightCap);

        GuiSlotEffect.getEffectsForInventory(SlotType.quick, inventoryEffects).forEach(this::addChild);
    }
}
