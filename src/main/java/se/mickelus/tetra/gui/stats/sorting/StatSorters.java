package se.mickelus.tetra.gui.stats.sorting;

import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.ToolTypes;
import se.mickelus.tetra.gui.stats.GuiStats;
import se.mickelus.tetra.gui.stats.getter.StatFormat;
import se.mickelus.tetra.gui.stats.getter.StatGetterToolEfficiency;
import se.mickelus.tetra.gui.stats.getter.StatGetterToolLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatSorters {

    public static final IStatSorter none = new NaturalSorter();
    public static final List<IStatSorter> sorters = new ArrayList<>(Arrays.asList(
            none,
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
