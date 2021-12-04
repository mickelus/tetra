package se.mickelus.tetra.items.modular.impl.holo.gui.craft;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolActions;
import se.mickelus.mutil.gui.GuiAlignment;
import se.mickelus.mutil.gui.GuiAttachment;
import se.mickelus.mutil.gui.GuiElement;
import se.mickelus.tetra.TetraToolActions;
import se.mickelus.tetra.gui.stats.AbilityStats;
import se.mickelus.tetra.gui.stats.GuiStats;
import se.mickelus.tetra.gui.stats.StatsHelper;
import se.mickelus.tetra.gui.stats.bar.GuiStatBarTool;
import se.mickelus.tetra.gui.stats.bar.GuiStatBase;
import se.mickelus.tetra.items.modular.IModularItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@ParametersAreNonnullByDefault
public class HoloStatsGui extends GuiElement {

    private static final List<GuiStatBase> bars = new ArrayList<>(Arrays.asList(
            GuiStats.integrity,
            GuiStats.attackDamageNormalized,
            GuiStats.drawStrength,
            GuiStats.abilityDamage,
            GuiStats.attackSpeedNormalized,
            GuiStats.drawSpeedNormalized,
            GuiStats.abilityCooldownNormalized,
            GuiStats.reachNormalized,
            GuiStats.durability,
            GuiStats.armor,
            GuiStats.toughness,
            GuiStats.shieldbreaker,
            GuiStats.blocking,
            GuiStats.blockingReflect,
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
            GuiStats.counterweight,
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
            GuiStats.intuit,
            GuiStats.magicCapacity,
            GuiStats.stability,
            GuiStats.workable,
            new GuiStatBarTool(0, 0, StatsHelper.barLength, TetraToolActions.hammer, true),
            new GuiStatBarTool(0, 0, StatsHelper.barLength, ToolActions.AXE_DIG, true),
            new GuiStatBarTool(0, 0, StatsHelper.barLength, ToolActions.PICKAXE_DIG, true),
            new GuiStatBarTool(0, 0, StatsHelper.barLength, ToolActions.SHOVEL_DIG, true),
            new GuiStatBarTool(0, 0, StatsHelper.barLength, TetraToolActions.cut, true),
            new GuiStatBarTool(0, 0, StatsHelper.barLength, TetraToolActions.pry, true),
            new GuiStatBarTool(0, 0, StatsHelper.barLength, ToolActions.HOE_DIG, true)
    ));

    private GuiElement barGroup;

    public HoloStatsGui(int x, int y) {
        super(x, y, 320, 52);

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

    private void realignBar(GuiStatBase bar, int index) {
        bar.setAttachment(GuiAttachment.topLeft);
        bar.setAlignment(GuiAlignment.left);

        bar.setX((index % 5) * (StatsHelper.barLength + 9));
        bar.setY(17 * ((index / 5)));
    }

    public void realignBars() {
        for (int i = 0; i < barGroup.getNumChildren(); i++) {
            realignBar((GuiStatBase) barGroup.getChild(i), i);
        }
    }
}
