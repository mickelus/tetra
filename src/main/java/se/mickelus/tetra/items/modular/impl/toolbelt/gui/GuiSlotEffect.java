package se.mickelus.tetra.items.modular.impl.toolbelt.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import se.mickelus.tetra.TetraMod;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.GuiString;
import se.mickelus.mgui.gui.GuiTexture;
import se.mickelus.tetra.items.modular.impl.toolbelt.SlotType;
import se.mickelus.tetra.module.ItemEffect;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GuiSlotEffect extends GuiElement {
    private static final ResourceLocation texture = new ResourceLocation(TetraMod.MOD_ID, "textures/gui/toolbelt-inventory.png");

    String tooltip;

    public GuiSlotEffect(int x, int y, SlotType slotType, ItemEffect effect) {
        super(x, y, 8, 8);

        tooltip = I18n.format(String.format("tetra.toolbelt.effect.tooltip.%s.%s", slotType.toString(), effect.toString()));

        switch (effect) {
            case quickAccess:
                addChild(new GuiTexture(0, 0, 8, 8, 0, 64, texture).setColor(0xbbbbbb));
                break;
            case cellSocket:
                addChild(new GuiTexture(0, 0, 8, 8, 8, 64, texture).setColor(0xbbbbbb));
                break;
            default:
                addChild(new GuiString(0, 0, "?"));
                break;
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

        // todo: this feels dirty :I
        AtomicInteger i = new AtomicInteger(0);
        return inventoryEffects.stream()
                .map(slotEffects -> {
                    GuiElement group = new GuiElement(i.getAndIncrement() * 17, 1, 16, 8);
                    getEffectsForSlot(slotType, slotEffects).forEach(group::addChild);
                    return group;
                })
                .collect(Collectors.toList());
    }
}
