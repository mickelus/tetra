package se.mickelus.tetra.items.modular.impl.toolbelt.gui.overlay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.GuiString;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.PotionsInventory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class PotionGroupGui extends GuiElement {
    GuiString focusSlot;
    PotionsInventory inventory;
    private PotionItemGui[] slots = new PotionItemGui[0];

    public PotionGroupGui(int x, int y) {
        super(x, y, 0, 0);

        focusSlot = new GuiString(0, -15, "");
        focusSlot.setAttachmentPoint(GuiAttachment.topCenter);
        focusSlot.setAttachmentAnchor(GuiAttachment.topCenter);
    }

    public void setInventory(PotionsInventory inventory) {
        clearChildren();
        this.inventory = inventory;
        int numSlots = inventory.getContainerSize();
        slots = new PotionItemGui[numSlots];

        focusSlot.setString("");
        addChild(focusSlot);

        width = 66;

        if (numSlots > 5) {
            height = 44;
        } else if (numSlots > 3) {
            height = 33;
        } else {
            width = numSlots * 22;
            height = 22;
        }

        for (int i = 0; i < numSlots; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (!itemStack.isEmpty()) {
                if (i > 6) {
                    slots[i] = new PotionItemGui(22, 22, itemStack, i, true);
                } else if (i > 4) {
                    slots[i] = new PotionItemGui((i - 5) * 22 + 11, -11, itemStack, i, true);
                } else if (i > 2) {
                    slots[i] = new PotionItemGui((i - 3) * 22 + 11, 11, itemStack, i, true);
                } else {
                    slots[i] = new PotionItemGui(i * 22, 0, itemStack, i, true);
                }
                addChild(slots[i]);
            }
        }
    }

    public void clear() {
        clearChildren();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            Arrays.stream(slots)
                    .filter(Objects::nonNull)
                    .forEach(item -> item.setVisible(true));
        } else {
            Arrays.stream(slots)
                    .filter(Objects::nonNull)
                    .forEach(item -> item.setVisible(false));
        }
        focusSlot.setVisible(visible);
    }

    @Override
    public void draw(GuiGraphics graphics, int refX, int refY, int screenWidth, int screenHeight, int mouseX, int mouseY, float opacity) {
        super.draw(graphics, refX, refY, screenWidth, screenHeight, mouseX, mouseY, opacity);

        int focus = getFocus();
        if (focus != -1) {
            focusSlot.setString(inventory.getItem(focus).getHoverName().getString());
        } else {
            focusSlot.setString("");
        }
    }

    public int getFocus() {
        for (int i = 0; i < slots.length; i++) {
            PotionItemGui element = slots[i];
            if (element != null && element.hasFocus()) {
                return element.getSlot();
            }
        }
        return -1;
    }

    public InteractionHand getHand() {
        return null;
    }
}
