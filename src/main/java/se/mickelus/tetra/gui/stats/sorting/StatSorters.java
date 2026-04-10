package se.mickelus.tetra.gui.stats.sorting;

import net.neoforged.neoforge.common.ItemAbilities;
import se.mickelus.tetra.TetraItemAbilities;
import se.mickelus.tetra.gui.stats.GuiStats;
import se.mickelus.tetra.gui.stats.getter.StatFormat;
import se.mickelus.tetra.gui.stats.getter.StatGetterIntegrity;
import se.mickelus.tetra.gui.stats.getter.StatGetterToolEfficiency;
import se.mickelus.tetra.gui.stats.getter.StatGetterToolLevel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
public class StatSorters {

    public static final IStatSorter none = new NaturalSorter();
    public static final List<IStatSorter> staticSorters = new ArrayList<>();
    public static List<IStatSorter> derivedSorters = Collections.emptyList();

    public static void initializeStaticSorters() {
        staticSorters.addAll(Arrays.asList(
                none,
                new BasicStatSorter(GuiStats.counterweightGetter, "tetra.stats.counterweight", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.drawStrengthGetter, "tetra.stats.draw_strength", StatFormat.oneDecimal),
                new BasicStatSorter(GuiStats.drawSpeedGetter, "tetra.stats.draw_speed", StatFormat.oneDecimal),
                new BasicStatSorter(GuiStats.abilityDamageGetter, "tetra.stats.ability_damage", StatFormat.oneDecimal),
                new BasicStatSorter(GuiStats.abilityCooldownGetter, "tetra.stats.ability_speed", StatFormat.oneDecimal),
                new BasicStatSorter(GuiStats.armorGetter, "tetra.stats.armor", StatFormat.oneDecimal),
                new BasicStatSorter(GuiStats.toughnessGetter, "tetra.stats.toughness", StatFormat.oneDecimal),
                new BasicStatSorter(new StatGetterToolLevel(TetraItemAbilities.hammer), "tetra.tool.hammer_dig", StatFormat.noDecimal).setSuffix("tetra.stats.level_suffix"),
                new BasicStatSorter(new StatGetterToolEfficiency(TetraItemAbilities.hammer), "tetra.tool.hammer_dig", StatFormat.noDecimal).setSuffix("tetra.stats.efficiency_suffix"),
                new BasicStatSorter(new StatGetterToolLevel(ItemAbilities.AXE_DIG), "tetra.tool.axe_dig", StatFormat.noDecimal).setSuffix("tetra.stats.level_suffix"),
                new BasicStatSorter(new StatGetterToolEfficiency(ItemAbilities.AXE_DIG), "tetra.tool.axe_dig", StatFormat.noDecimal).setSuffix("tetra.stats.efficiency_suffix"),
                new BasicStatSorter(new StatGetterToolLevel(ItemAbilities.PICKAXE_DIG), "tetra.tool.pickaxe_dig", StatFormat.noDecimal).setSuffix("tetra.stats.level_suffix"),
                new BasicStatSorter(new StatGetterToolEfficiency(ItemAbilities.PICKAXE_DIG), "tetra.tool.pickaxe_dig", StatFormat.noDecimal).setSuffix("tetra.stats.efficiency_suffix"),
                new BasicStatSorter(new StatGetterToolLevel(ItemAbilities.SHOVEL_DIG), "tetra.tool.shovel_dig", StatFormat.noDecimal).setSuffix("tetra.stats.level_suffix"),
                new BasicStatSorter(new StatGetterToolEfficiency(ItemAbilities.SHOVEL_DIG), "tetra.tool.shovel_dig", StatFormat.noDecimal).setSuffix("tetra.stats.efficiency_suffix"),
                new BasicStatSorter(new StatGetterToolLevel(TetraItemAbilities.cut), "tetra.tool.cut", StatFormat.noDecimal).setSuffix("tetra.stats.level_suffix"),
                new BasicStatSorter(new StatGetterToolEfficiency(TetraItemAbilities.cut), "tetra.tool.cut", StatFormat.noDecimal).setSuffix("tetra.stats.efficiency_suffix"),
                new BasicStatSorter(new StatGetterToolLevel(TetraItemAbilities.pry), "tetra.tool.pry", StatFormat.noDecimal).setSuffix("tetra.stats.level_suffix"),
                new BasicStatSorter(new StatGetterToolEfficiency(TetraItemAbilities.pry), "tetra.tool.pry", StatFormat.noDecimal).setSuffix("tetra.stats.efficiency_suffix"),
                new BasicStatSorter(new StatGetterToolLevel(ItemAbilities.HOE_DIG), "tetra.tool.hoe_dig", StatFormat.noDecimal).setSuffix("tetra.stats.level_suffix"),
                new BasicStatSorter(new StatGetterToolEfficiency(ItemAbilities.HOE_DIG), "tetra.tool.hoe_dig", StatFormat.noDecimal).setSuffix("tetra.stats.efficiency_suffix"),
                new BasicStatSorter(GuiStats.reachGetter, "tetra.stats.reach", StatFormat.oneDecimal),
                new BasicStatSorter(GuiStats.attackRangeGetter, "tetra.attack_range.reach", StatFormat.oneDecimal),
                new BasicStatSorter(GuiStats.blockingReflectGetter, "tetra.stats.blocking_reflect", StatFormat.oneDecimal),
                new BasicStatSorter(GuiStats.bashingGetter, "tetra.stats.bashing", StatFormat.oneDecimal),
                new BasicStatSorter(GuiStats.throwableGetter, "tetra.stats.throwable", StatFormat.oneDecimal),
                new BasicStatSorter(GuiStats.ricochetGetter, "tetra.stats.ricochet", StatFormat.oneDecimal),
                new BasicStatSorter(GuiStats.piercingGetter, "tetra.stats.piercing", StatFormat.oneDecimal),
                new BasicStatSorter(GuiStats.jabGetter, "tetra.stats.jab", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.quickslotGetter, "tetra.stats.toolbelt.quickslot", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.potionStorageGetter, "tetra.stats.toolbelt.potion_storage", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.storageGetter, "tetra.stats.toolbelt.storage", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.quiverGetter, "tetra.stats.toolbelt.quiver", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.boosterGetter, "tetra.stats.toolbelt.booster", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.sweepingGetter, "tetra.stats.sweeping", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.bleedingGetter, "tetra.stats.bleeding", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.backstabGetter, "tetra.stats.backstab", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.armorPenetrationGetter, "tetra.stats.armorPenetration", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.crushingGetter, "tetra.stats.crushing", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.skeweringGetter, "tetra.stats.skewering", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.severingGetter, "tetra.stats.severing", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.stunGetter, "tetra.stats.stun", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.howlingGetter, "tetra.stats.howling", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.fierySelfGetter, "tetra.stats.fierySelf", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.enderReverbGetter, "tetra.stats.enderReverb", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.criticalGetter, "tetra.stats.criticalStrike", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.intuitGetter, "tetra.stats.intuit", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.earthbindGetter, "tetra.stats.earthbind", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.releaseLatchGetter, "tetra.stats.bow.releaseLatch", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.overbowedGetter, "tetra.stats.bow.overbowed", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.multishotGetter, "tetra.stats.multishot", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.zoomGetter, "tetra.stats.zoom", StatFormat.oneDecimal),
                new BasicStatSorter(GuiStats.spreadGetter, "tetra.stats.spread", StatFormat.oneDecimal),
                new BasicStatSorter(GuiStats.focusGetter, "tetra.stats.focus", StatFormat.oneDecimal),
                new BasicStatSorter(GuiStats.velocityGetter, "tetra.stats.velocity", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.sweeperRangeGetter, "tetra.stats.holo.sweeperRange", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.sweeperHorizontalSpreadGetter, "tetra.stats.holo.sweeperHorizontalSpread", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.sweeperVerticalSpreadGetter, "tetra.stats.holo.sweeperVerticalSpread", StatFormat.noDecimal),
                new BasicStatSorter(new StatGetterIntegrity(), "tetra.stats.integrity", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.durabilityGetter, "tetra.stats.durability", StatFormat.abbreviate),
                new BasicStatSorter(GuiStats.magicCapacityGetter, "tetra.stats.magicCapacity", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.stabilityGetter, "tetra.stats.stability", StatFormat.noDecimal),
                new BasicStatSorter(GuiStats.workableGetter, "tetra.stats.workable", StatFormat.noDecimal)
        ));
    }

    public static void setDerivedSorters(List<IStatSorter> sorters) {
        derivedSorters = sorters;
    }
}
