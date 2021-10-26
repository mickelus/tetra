package se.mickelus.tetra.gui.stats.sorting;

import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.gui.stats.GuiStats;
import se.mickelus.tetra.gui.stats.getter.StatFormat;
import se.mickelus.tetra.gui.stats.getter.StatGetterIntegrity;
import se.mickelus.tetra.gui.stats.getter.StatGetterToolEfficiency;
import se.mickelus.tetra.gui.stats.getter.StatGetterToolLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatSorters {

    public static final IStatSorter none = new NaturalSorter();
    public static final List<IStatSorter> sorters = new ArrayList<>(Arrays.asList(
            none,
            new BasicStatSorter(new StatGetterIntegrity(), "tetra.stats.integrity", StatFormat.noDecimal),
            new BasicStatSorter(GuiStats.attackDamageNormalizedGetter, "tetra.stats.attack_damage_normalized", StatFormat.oneDecimal),
            new BasicStatSorter(GuiStats.counterweightGetter, "tetra.stats.counterweight", StatFormat.noDecimal),
            new BasicStatSorter(GuiStats.attackSpeedGetterNormalized, "tetra.stats.speed_normalized", StatFormat.oneDecimal),
            new BasicStatSorter(GuiStats.drawStrengthGetter, "tetra.stats.draw_strength", StatFormat.oneDecimal),
            new BasicStatSorter(GuiStats.drawSpeedGetter, "tetra.stats.draw_speed", StatFormat.oneDecimal),
            new BasicStatSorter(GuiStats.abilityDamageGetter, "tetra.stats.ability_damage", StatFormat.oneDecimal),
            new BasicStatSorter(GuiStats.abilityCooldownGetter, "tetra.stats.ability_speed", StatFormat.oneDecimal),
            new BasicStatSorter(GuiStats.reachGetterNormalized, "tetra.stats.reach_normalized", StatFormat.oneDecimal),
            new BasicStatSorter(GuiStats.durabilityGetter, "tetra.stats.durability", StatFormat.abbreviate),
            new BasicStatSorter(GuiStats.armorGetter, "tetra.stats.armor", StatFormat.oneDecimal),
            new BasicStatSorter(GuiStats.toughnessGetter, "tetra.stats.toughness", StatFormat.oneDecimal),
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
            new BasicStatSorter(GuiStats.zoomGetter, "tetra.stats.zoom", StatFormat.noDecimal),
            new BasicStatSorter(GuiStats.velocityGetter, "tetra.stats.velocity", StatFormat.noDecimal),
            new BasicStatSorter(GuiStats.magicCapacityGetter, "tetra.stats.magicCapacity", StatFormat.noDecimal),
            new BasicStatSorter(GuiStats.stabilityGetter, "tetra.stats.stability", StatFormat.noDecimal),
            new BasicStatSorter(GuiStats.workableGetter, "tetra.stats.workable", StatFormat.noDecimal),
            new BasicStatSorter(GuiStats.scannerRangeGetter, "tetra.stats.holo.scannerRange", StatFormat.noDecimal),
            new BasicStatSorter(GuiStats.scannerHorizontalSpreadGetter, "tetra.stats.holo.scannerHorizontalSpread", StatFormat.noDecimal),
            new BasicStatSorter(GuiStats.scannerVerticalSpreadGetter, "tetra.stats.holo.scannerVerticalSpread", StatFormat.noDecimal),
            new BasicStatSorter(new StatGetterToolLevel(ToolTypes.hammer), "tetra.tool.hammer", StatFormat.noDecimal).setSuffix("tetra.stats.level_suffix"),
            new BasicStatSorter(new StatGetterToolLevel(ToolType.AXE), "tetra.tool.axe", StatFormat.noDecimal).setSuffix("tetra.stats.level_suffix"),
            new BasicStatSorter(new StatGetterToolLevel(ToolType.PICKAXE), "tetra.tool.pickaxe", StatFormat.noDecimal).setSuffix("tetra.stats.level_suffix"),
            new BasicStatSorter(new StatGetterToolLevel(ToolType.SHOVEL), "tetra.tool.shovel", StatFormat.noDecimal).setSuffix("tetra.stats.level_suffix"),
            new BasicStatSorter(new StatGetterToolLevel(ToolTypes.cut), "tetra.tool.cut", StatFormat.noDecimal).setSuffix("tetra.stats.level_suffix"),
            new BasicStatSorter(new StatGetterToolLevel(ToolTypes.pry), "tetra.tool.pry", StatFormat.noDecimal).setSuffix("tetra.stats.level_suffix"),
            new BasicStatSorter(new StatGetterToolLevel(ToolType.HOE), "tetra.tool.hoe", StatFormat.noDecimal).setSuffix("tetra.stats.level_suffix"),
            new BasicStatSorter(new StatGetterToolEfficiency(ToolTypes.hammer), "tetra.tool.hammer", StatFormat.noDecimal).setSuffix("tetra.stats.efficiency_suffix"),
            new BasicStatSorter(new StatGetterToolEfficiency(ToolType.AXE), "tetra.tool.axe", StatFormat.noDecimal).setSuffix("tetra.stats.efficiency_suffix"),
            new BasicStatSorter(new StatGetterToolEfficiency(ToolType.PICKAXE), "tetra.tool.pickaxe", StatFormat.noDecimal).setSuffix("tetra.stats.efficiency_suffix"),
            new BasicStatSorter(new StatGetterToolEfficiency(ToolType.SHOVEL), "tetra.tool.shovel", StatFormat.noDecimal).setSuffix("tetra.stats.efficiency_suffix"),
            new BasicStatSorter(new StatGetterToolEfficiency(ToolTypes.cut), "tetra.tool.cut", StatFormat.noDecimal).setSuffix("tetra.stats.efficiency_suffix"),
            new BasicStatSorter(new StatGetterToolEfficiency(ToolTypes.pry), "tetra.tool.pry", StatFormat.noDecimal).setSuffix("tetra.stats.efficiency_suffix"),
            new BasicStatSorter(new StatGetterToolEfficiency(ToolType.HOE), "tetra.tool.hoe", StatFormat.noDecimal).setSuffix("tetra.stats.efficiency_suffix")
    ));
}
