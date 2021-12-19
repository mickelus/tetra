package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiRect;
import se.mickelus.mutil.gui.GuiTexture;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.GuiColors;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.impl.toolbelt.SlotType;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.StorageInventory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
public class GuiStorageBackdrop extends GuiElement {
    public GuiStorageBackdrop(int x, int y, int numSlots, Collection<Collection<ItemEffect>> inventoryEffects) {
        super(x, y, 0, 0);

        setAttachmentPoint(GuiAttachment.bottomCenter);
        setAttachmentAnchor(GuiAttachment.topCenter);

        int maxCols = StorageInventory.getColumns(numSlots);
        int cols = Math.min(numSlots, maxCols);
        int rows = 1 + (numSlots - 1) / cols;

        setWidth(cols * 17 - 9);
        setHeight(rows * 17 + 11);

        // background rects
        addChild(new GuiRect(0, 3, width, height - 6, 0));
        addChild(new GuiRect(0, 4, width, 1, GuiColors.mutedStrong));
        addChild(new GuiRect(0, -4, width, 1, GuiColors.muted).setAttachment(GuiAttachment.bottomLeft));

        // more background rects
        if (height > 28) {
            addChild(new GuiRect(-7, 14, width + 14, height - 28, 0));
            addChild(new GuiRect(-6, 14, 1, height - 28, GuiColors.mutedStrong));
            addChild(new GuiRect(6, 14, 1, height - 28, GuiColors.mutedStrong).setAttachment(GuiAttachment.topRight));

            for (int i = 0; i < cols - 1; i++) {
                for (int j = 0; j < rows - 1; j++) {
                    addChild(new GuiRect(i * 17 - 3 + 12, j * 17 + 22, 7, 1, GuiColors.mutedStrong));
                    addChild(new GuiRect(i * 17 + 12, j * 17 - 3 + 22, 1, 7, GuiColors.mutedStrong));
                    addChild(new GuiRect(i * 17 + 11, j * 17 + 21, 3, 3, 0));
                }
            }
        }

        // top-left texture
        addChild(new GuiTexture(0, 0, 16, 14, 32, 0, GuiTextures.toolbelt).setAttachmentPoint(GuiAttachment.topRight));

        // bottom-left texture
        addChild(new GuiTexture(0, 0, 16, 14, 32, 14, GuiTextures.toolbelt)
                .setAttachmentPoint(GuiAttachment.bottomRight)
                .setAttachmentAnchor(GuiAttachment.bottomLeft));

        // top-right texture
        addChild(new GuiTexture(0, 0, 16, 14, 48, 0, GuiTextures.toolbelt)
                .setAttachmentPoint(GuiAttachment.topLeft)
                .setAttachmentAnchor(GuiAttachment.topRight));

        // bottom-right texture
        addChild(new GuiTexture(0, 0, 16, 14, 48, 14, GuiTextures.toolbelt)
                .setAttachmentPoint(GuiAttachment.bottomLeft)
                .setAttachmentAnchor(GuiAttachment.bottomRight));

        GuiSlotEffect.getEffectsForInventory(SlotType.storage, inventoryEffects, cols).forEach(this::addChild);
    }
}
