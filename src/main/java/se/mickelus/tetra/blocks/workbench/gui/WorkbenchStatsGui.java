package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolActions;
import se.mickelus.mutil.gui.GuiAlignment;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.mutil.gui.animation.Applier;
import se.mickelus.mutil.gui.animation.KeyframeAnimation;
import se.mickelus.tetra.TetraToolActions;
import se.mickelus.tetra.gui.stats.AbilityStats;
import se.mickelus.tetra.gui.stats.GuiStats;
import se.mickelus.tetra.gui.stats.StatsHelper;
import se.mickelus.tetra.gui.stats.bar.GuiStatBarTool;
import se.mickelus.tetra.gui.stats.bar.GuiStatBase;
import se.mickelus.tetra.items.modular.IModularItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@ParametersAreNonnullByDefault
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
            GuiStats.shieldbreaker,
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
            GuiStats.suspendSelf,
            GuiStats.sweeping,
            GuiStats.bleeding,
            GuiStats.backstab,
            GuiStats.armorPenetration,
            GuiStats.crushing,
            GuiStats.skewering,
            GuiStats.howling,
            GuiStats.knockback,
            AbilityStats.execute,
            GuiStats.severing,
            GuiStats.stun,
            AbilityStats.lunge,
            AbilityStats.slam,
            AbilityStats.puncture,
            AbilityStats.pry,
            AbilityStats.overpower,
            AbilityStats.reap,
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
            new GuiStatBarTool(0, 0, StatsHelper.barLength, TetraToolActions.hammer),
            new GuiStatBarTool(0, 0, StatsHelper.barLength, TetraToolActions.cut),
            new GuiStatBarTool(0, 0, StatsHelper.barLength, ToolActions.AXE_DIG),
            new GuiStatBarTool(0, 0, StatsHelper.barLength, ToolActions.PICKAXE_DIG),
            new GuiStatBarTool(0, 0, StatsHelper.barLength, ToolActions.SHOVEL_DIG),
            new GuiStatBarTool(0, 0, StatsHelper.barLength, ToolActions.SWORD_DIG),
            new GuiStatBarTool(0, 0, StatsHelper.barLength, TetraToolActions.pry),
            new GuiStatBarTool(0, 0, StatsHelper.barLength, ToolActions.HOE_DIG)
    ));
    private final GuiElement barGroup;

    public WorkbenchStatsGui(int x, int y) {
        super(x, y, 200, 52);

        barGroup = new GuiElement(0, 0, width, height);
        addChild(barGroup);
    }

    public static void addBar(GuiStatBase statBar) {
        bars.add(statBar);
    }

    public void update(ItemStack itemStack, ItemStack previewStack, String slot, String improvement, Player player) {
        boolean shouldShow = !itemStack.isEmpty() && itemStack.getItem() instanceof IModularItem;
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

    public void realignBars() {
        List<GuiStatBase> bars = barGroup.getChildren(GuiStatBase.class);
        for (int i = 0; i < bars.size(); i++) {
            realignBar(bars.get(i), i);
        }

    }

    private void realignBar(GuiStatBase bar, int index) {
        bar.setY(-17 * ((index % 6) / 2) - 3);
        bar.setAttachmentAnchor(GuiAttachment.bottomCenter);

        int xOffset = 3 + (index / 6) * (StatsHelper.barLength + 3);
        if (index % 2 == 0) {
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
