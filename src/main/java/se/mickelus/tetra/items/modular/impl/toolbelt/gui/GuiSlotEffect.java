package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import net.minecraft.client.resources.language.I18n;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.gui.GuiTextures;
import se.mickelus.tetra.items.modular.impl.toolbelt.SlotType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
@ParametersAreNonnullByDefault
public class GuiSlotEffect extends GuiElement {
    String tooltip;

    public GuiSlotEffect(int x, int y, SlotType slotType, ItemEffect effect) {
        super(x, y, 8, 8);

        tooltip = I18n.get(String.format("tetra.toolbelt.effect.tooltip.%s.%s", slotType.toString(), effect.getKey()));

        if (ItemEffect.quickAccess.equals(effect)) {
            addChild(new GuiTexture(0, 0, 8, 8, 0, 64, GuiTextures.toolbelt).setColor(0xbbbbbb));
        } else if (ItemEffect.cellSocket.equals(effect)) {
            addChild(new GuiTexture(0, 0, 8, 8, 8, 64, GuiTextures.toolbelt).setColor(0xbbbbbb));
        } else {
            addChild(new GuiString(0, 0, "?"));
        }
    }

    @Override
    protected void calculateFocusState(int refX, int refY, int mouseX, int mouseY) {
        super.calculateFocusState(refX, refY, mouseX, mouseY);
    }

    @Override
    public List<String> getTooltipLines() {
        if (hasFocus()) {
            return Collections.singletonList(tooltip);
        }
        return super.getTooltipLines();
    }

    public static Collection<GuiSlotEffect> getEffectsForSlot(SlotType slotType, Collection<ItemEffect> slotEffects) {
        int offset = 4 - slotEffects.size() * 4;

        // todo: this feels dirty :I
        AtomicInteger i = new AtomicInteger(0);
        return slotEffects.stream()
                .map(effect -> new GuiSlotEffect(8 * i.getAndIncrement() + offset, 0, slotType, effect))
                .collect(Collectors.toList());
    }

    public static Collection<GuiElement> getEffectsForInventory(SlotType slotType, Collection<Collection<ItemEffect>> inventoryEffects) {
        return getEffectsForInventory(slotType, inventoryEffects, Integer.MAX_VALUE);
    }

    public static Collection<GuiElement> getEffectsForInventory(SlotType slotType, Collection<Collection<ItemEffect>> inventoryEffects, int columns) {

        // todo: this feels dirty :I
        AtomicInteger i = new AtomicInteger(0);
        return inventoryEffects.stream()
                .map(slotEffects -> {
                    GuiElement group = new GuiElement((i.get() % columns) * 17, -19 - (i.getAndIncrement() / columns) * 17, 16, 8);
                    group.setAttachment(GuiAttachment.bottomLeft);
                    getEffectsForSlot(slotType, slotEffects).forEach(group::addChild);
                    return group;
                })
                .collect(Collectors.toList());
    }
}
