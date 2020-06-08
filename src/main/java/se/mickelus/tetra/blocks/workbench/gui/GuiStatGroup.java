package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.mgui.gui.GuiAlignment;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.gui.statbar.GuiStatBarCapability;
import se.mickelus.tetra.gui.statbar.GuiStatBase;
import se.mickelus.tetra.gui.statbar.GuiStats;
import se.mickelus.tetra.items.modular.ItemModular;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class GuiStatGroup extends GuiElement {

    private List<GuiStatBase> bars;
    private GuiElement barGroup;

    public GuiStatGroup(int x, int y) {
        super(x, y, 200, 52);

        bars = new LinkedList<>();

        barGroup = new GuiElement(0, 0, width, height);
        addChild(barGroup);

        bars.add(GuiStats.damage);
        bars.add(GuiStats.speed);
        bars.add(GuiStats.rangedDamage);
        bars.add(GuiStats.rangedSpeed);
        bars.add(GuiStats.shieldDamage);
        bars.add(GuiStats.shieldSpeed);
        bars.add(GuiStats.reach);
        bars.add(GuiStats.durability);
        bars.add(GuiStats.armor);
        bars.add(GuiStats.toughness);
        bars.add(GuiStats.blockingDuration);
        bars.add(GuiStats.bashing);
        bars.add(GuiStats.throwable);
        bars.add(GuiStats.quickslot);
        bars.add(GuiStats.potion_storage);
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

        Arrays.stream(Capability.values())
                .map(capability -> new GuiStatBarCapability(0, 0, GuiStats.barLength, capability))
                .forEach(bars::add);

        bars.forEach(bar -> bar.setAttachmentAnchor(GuiAttachment.bottomCenter));
    }

    public void update(ItemStack itemStack, ItemStack previewStack, String slot, String improvement, PlayerEntity player) {
        boolean shouldShow = !itemStack.isEmpty() && itemStack.getItem() instanceof ItemModular;
        setVisible(shouldShow);
        if (shouldShow) {
            barGroup.clearChildren();
            bars.stream()
                    .filter(bar -> bar.shouldShow(player, itemStack, previewStack, slot, improvement))
                    .forEach(bar -> {
                        bar.update(player, itemStack, previewStack, slot, improvement);

                        realignBar(bar);
                        barGroup.addChild(bar);
                    });

        }
    }

    public void showAnimation() {
        if (isVisible()) {
            for (int i = 0; i < barGroup.getNumChildren(); i++) {
                // we run this to clear animations if the player managed to hide the bars before the animations finished
                barGroup.getChild(i).updateAnimations();

                new KeyframeAnimation(100, barGroup.getChild(i))
                        .withDelay(i * 60 + 400)
                        .applyTo(new Applier.Opacity(0, 1), new Applier.TranslateY(3, 0, true))
                        .start();
            }
        }
    }

    private void realignBar(GuiStatBase bar) {
        int count = barGroup.getNumChildren();

        bar.setY(-17 * ((count % 6) / 2) - 3);

        int xOffset = 3 + (count / 6) * (GuiStats.barLength + 3);
        if (count % 2 == 0) {
            bar.setX(xOffset);
            bar.setAttachmentPoint(GuiAttachment.bottomLeft);
            bar.setAlignment(GuiAlignment.left);
        } else {
            bar.setX(-xOffset);
            bar.setAttachmentPoint(GuiAttachment.bottomRight);
            bar.setAlignment(GuiAlignment.right);
        }
    }

}
