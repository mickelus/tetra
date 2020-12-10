package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ToolType;
import se.mickelus.mgui.gui.GuiAlignment;
import se.mickelus.mgui.gui.GuiAttachment;
import se.mickelus.mgui.gui.GuiElement;
import se.mickelus.mgui.gui.animation.Applier;
import se.mickelus.mgui.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.gui.statbar.GuiStatBarTool;
import se.mickelus.tetra.gui.statbar.GuiStatBase;
import se.mickelus.tetra.gui.statbar.GuiStats;
import se.mickelus.tetra.items.modular.ModularItem;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;


public class WorkbenchStatsGui extends GuiElement {

    private static final List<GuiStatBase> bars = new LinkedList<>(Arrays.asList(
            GuiStats.attackDamage,
            GuiStats.attackSpeed,
            GuiStats.drawStrength,
            GuiStats.drawSpeed,
            GuiStats.abilityDamage,
            GuiStats.abilityCooldown,
            GuiStats.reach,
            GuiStats.durability,
            GuiStats.armor,
            GuiStats.toughness,
            GuiStats.blocking,
            GuiStats.bashing,
            GuiStats.throwable,
            GuiStats.ricochet,
            GuiStats.piercing,
            GuiStats.jab,
            GuiStats.quickslot,
            GuiStats.potionStorage,
            GuiStats.storage,
            GuiStats.quiver,
            GuiStats.booster,
            GuiStats.sweeping,
            GuiStats.bleeding,
            GuiStats.backstab,
            GuiStats.armorPenetration,
            GuiStats.unarmoredDamage,
            GuiStats.knockback,
            GuiStats.looting,
            GuiStats.fiery,
            GuiStats.smite,
            GuiStats.arthropod,
            GuiStats.unbreaking,
            GuiStats.mending,
            GuiStats.silkTouch,
            GuiStats.fortune,
            GuiStats.infinity,
            GuiStats.flame,
            GuiStats.punch,
            GuiStats.quickStrike,
            GuiStats.softStrike,
            GuiStats.fierySelf,
            GuiStats.enderReverb,
            GuiStats.criticalStrike,
            GuiStats.earthbind,
            GuiStats.releaseLatch,
            GuiStats.overbowed,
            GuiStats.multishot,
            GuiStats.zoom,
            GuiStats.velocity,
            GuiStats.scannerRange,
            GuiStats.scannerHorizontalSpread,
            GuiStats.scannerVerticalSpread,
            GuiStats.intuit,
            GuiStats.workable,
            GuiStats.stability,
            new GuiStatBarTool(0, 0, GuiStats.barLength, ToolTypes.hammer),
            new GuiStatBarTool(0, 0, GuiStats.barLength, ToolType.AXE),
            new GuiStatBarTool(0, 0, GuiStats.barLength, ToolType.PICKAXE),
            new GuiStatBarTool(0, 0, GuiStats.barLength, ToolType.SHOVEL),
            new GuiStatBarTool(0, 0, GuiStats.barLength, ToolTypes.cut),
            new GuiStatBarTool(0, 0, GuiStats.barLength, ToolTypes.pry),
            new GuiStatBarTool(0, 0, GuiStats.barLength, ToolType.HOE)
    ));
    private GuiElement barGroup;

    public WorkbenchStatsGui(int x, int y) {
        super(x, y, 200, 52);

        barGroup = new GuiElement(0, 0, width, height);
        addChild(barGroup);
    }

    public static void addBar(GuiStatBase statBar) {
        bars.add(statBar);
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
        bar.setAttachmentAnchor(GuiAttachment.bottomCenter);

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
