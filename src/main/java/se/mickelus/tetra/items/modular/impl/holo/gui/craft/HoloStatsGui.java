package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.mgui.gui.GuiAlignment;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.tetra.gui.statbar.GuiStatBarCapability;
import se.mickelus.tetra.gui.statbar.GuiStatBase;
import se.mickelus.tetra.gui.statbar.GuiStats;
import se.mickelus.tetra.items.modular.ModularItem;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class HoloStatsGui extends GuiElement {

    private List<GuiStatBase> bars;
    private GuiElement barGroup;

    public HoloStatsGui(int x, int y) {
        super(x, y, 200, 52);

        bars = new LinkedList<>();

        barGroup = new GuiElement(0, 0, width, height);
        addChild(barGroup);

        bars.add(GuiStats.integrity);
        bars.add(GuiStats.damage);
        bars.add(GuiStats.rangedDamage);
        bars.add(GuiStats.shieldDamage);
        bars.add(GuiStats.speedNormalized);
        bars.add(GuiStats.rangedSpeedNormalized);
        bars.add(GuiStats.shieldSpeedNormalized);
        bars.add(GuiStats.reach);
        bars.add(GuiStats.durability);
        bars.add(GuiStats.armor);
        bars.add(GuiStats.toughness);
        bars.add(GuiStats.blocking);
        bars.add(GuiStats.bashing);
        bars.add(GuiStats.throwable);
        bars.add(GuiStats.quickslot);
        bars.add(GuiStats.potionStorage);
        bars.add(GuiStats.storage);
        bars.add(GuiStats.quiver);
        bars.add(GuiStats.booster);
        bars.add(GuiStats.sweeping);
        bars.add(GuiStats.bleeding);
        bars.add(GuiStats.backstab);
        bars.add(GuiStats.armorPenetration);
        bars.add(GuiStats.unarmoredDamage);
        bars.add(GuiStats.knockback);
        bars.add(GuiStats.looting);
        bars.add(GuiStats.fiery);
        bars.add(GuiStats.smite);
        bars.add(GuiStats.arthropod);
        bars.add(GuiStats.unbreaking);
        bars.add(GuiStats.mending);
        bars.add(GuiStats.silkTouch);
        bars.add(GuiStats.fortune);
        bars.add(GuiStats.infinity);
        bars.add(GuiStats.flame);
        bars.add(GuiStats.punch);
        bars.add(GuiStats.quickStrike);
        bars.add(GuiStats.softStrike);
        bars.add(GuiStats.fierySelf);
        bars.add(GuiStats.enderReverb);
        bars.add(GuiStats.criticalStrike);
        bars.add(GuiStats.earthbind);
        bars.add(GuiStats.releaseLatch);
        bars.add(GuiStats.overbowed);
        bars.add(GuiStats.intuit);
        bars.add(GuiStats.magicCapacity);

        Arrays.stream(Capability.values())
                .map(capability -> new GuiStatBarCapability(0, 0, GuiStats.barLength, capability))
                .forEach(bars::add);

        bars.forEach(bar -> bar.setAttachmentAnchor(GuiAttachment.bottomCenter));
    }

    public void update(ItemStack itemStack, ItemStack previewStack, String slot, String improvement, PlayerEntity player) {
        boolean shouldShow = !itemStack.isEmpty() && itemStack.getItem() instanceof ModularItem;
        setVisible(shouldShow);
        if (shouldShow) {
            barGroup.clearChildren();
            bars.stream()
                    .filter(bar -> bar.shouldShow(player, itemStack, previewStack, slot, improvement))
                    .forEach(bar -> {
                        bar.update(player, itemStack, previewStack, slot, improvement);

                        realignBar(bar, barGroup.getNumChildren());
                        barGroup.addChild(bar);
                    });
        }
    }

    private void realignBar(GuiStatBase bar, int index) {
        bar.setAttachment(GuiAttachment.topLeft);
        bar.setAlignment(GuiAlignment.left);

        bar.setX((index % 3) * (GuiStats.barLength + 10));
        bar.setY(17 * ((index / 3)));
    }

    public void realignBars() {
        for (int i = 0; i < barGroup.getNumChildren(); i++) {
            realignBar((GuiStatBase) barGroup.getChild(i), i);
        }
    }
}
