package se.mickelus.tetra.gui.statbar;

import se.mickelus.tetra.effect.*;
import se.mickelus.tetra.gui.statbar.getter.*;
import static se.mickelus.tetra.gui.statbar.StatsHelper.*;

public class AbilityStats {
    public static final IStatGetter abilitySpeedGetter = new StatGetterEffectLevel(ItemEffect.abilitySpeed, 1);
    public static GuiStatIndicator abilitySpeedIndicator = new GuiStatIndicator(0, 0, "tetra.stats.ability_speed_bonus", 8,
            abilitySpeedGetter, new TooltipGetterPercentage("tetra.stats.ability_speed_bonus.tooltip", abilitySpeedGetter));

    public static final IStatGetter abilityDefensiveGetter = new StatGetterEffectLevel(ItemEffect.abilityDefensive, 1);
    public static final IStatGetter abilityDefEffGetter = new StatGetterEffectEfficiency(ItemEffect.abilityDefensive, 1);

    public static final IStatGetter abilityOverchargeGetter = new StatGetterEffectLevel(ItemEffect.abilityOvercharge, 1);
    public static final IStatGetter abilityOverchargeEffGetter = new StatGetterEffectEfficiency(ItemEffect.abilityOvercharge, 1);

    public static final IStatGetter abilityMomentumGetter = new StatGetterEffectLevel(ItemEffect.abilityMomentum, 1);
    public static final IStatGetter abilityMomentumEffGetter = new StatGetterEffectEfficiency(ItemEffect.abilityMomentum, 1);

    public static final IStatGetter abilityComboGetter = new StatGetterEffectLevel(ItemEffect.abilityCombo, 1);
    public static final IStatGetter abilityComboEffGetter = new StatGetterEffectEfficiency(ItemEffect.abilityCombo, 1);

    public static final IStatGetter abilityRevengeGetter = new StatGetterEffectLevel(ItemEffect.abilityRevenge, 1);
    public static final IStatGetter abilityRevengeEffGetter = new StatGetterEffectEfficiency(ItemEffect.abilityRevenge, 1);

    public static final IStatGetter abilityOverextendGetter = new StatGetterEffectLevel(ItemEffect.abilityOverextend, 1);
    public static final IStatGetter abilityOverextendEffGetter = new StatGetterEffectEfficiency(ItemEffect.abilityOverextend, 1);

    public static final IStatGetter lungeGetter = new StatGetterEffectLevel(ItemEffect.lunge, 1);
    public static final GuiStatBar lunge = new GuiStatBar(0, 0, barLength, "tetra.stats.lunge",
            0, 200, false, lungeGetter, LabelGetterBasic.percentageLabel,
            new TooltipGetterMultiValue("tetra.stats.lunge.tooltip",
                    withStats(lungeGetter, multiply(lungeGetter, new StatGetterAbilityDamage(0, 0.01)),
                            new StatGetterAbilityChargeTime(LungeEffect.instance), new StatGetterAbilityCooldown(LungeEffect.instance)),
                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.oneDecimal, StatFormat.oneDecimal)))
            .setIndicators(
                    abilitySpeedIndicator,
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_defensive", 9, abilityDefensiveGetter,
                            new TooltipGetterNone("tetra.stats.lunge_defensive.tooltip")),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_overcharge", 10, abilityOverchargeGetter,
                            new TooltipGetterMultiValue("tetra.stats.lunge_overcharge.tooltip",
                                    withStats(abilityOverchargeGetter, abilityOverchargeEffGetter,
                                            multiply(abilityOverchargeEffGetter, new StatGetterAbilityDamage(0, 0.01))),
                                    withFormat(StatFormat.noDecimal, StatFormat.noDecimal, StatFormat.oneDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_momentum", 11, abilityMomentumGetter,
                            new TooltipGetterMultiValue("tetra.stats.lunge_momentum.tooltip",
                                    withStats(abilityMomentumGetter, multiply(abilityMomentumGetter, abilityMomentumEffGetter)),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_combo", 12, abilityComboGetter,
                            new TooltipGetterMultiValue("tetra.stats.lunge_combo.tooltip",
                                    withStats(abilityComboGetter, abilityComboEffGetter),
                                    withFormat(StatFormat.noDecimal, StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_revenge", 13, abilityRevengeGetter,
                            new TooltipGetterNone("tetra.stats.lunge_revenge.tooltip")),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_overextend", 14, abilityOverextendGetter,
                            new TooltipGetterMultiValue("tetra.stats.lunge_overextend.tooltip",
                                    withStats(abilityOverextendGetter, multiply(abilityOverextendGetter, new StatGetterAbilityDamage(0, 0.01))),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal))));

    public static final IStatGetter executeGetter = new StatGetterEffectLevel(ItemEffect.execute, 0.1);
    public static final GuiStatBar execute = new GuiStatBar(0, 0, barLength, "tetra.stats.execute",
            0, 5, false, executeGetter, LabelGetterBasic.percentageLabelDecimal,
            new TooltipGetterMultiValue("tetra.stats.execute.tooltip",
                    withStats(new StatGetterAbilityDamage(), executeGetter,
                            new StatGetterEffectEfficiency(ItemEffect.execute, 1), new StatGetterAbilityChargeTime(ExecuteEffect.instance),
                            new StatGetterAbilityCooldown(ExecuteEffect.instance)),
                    withFormat(StatFormat.oneDecimal, StatFormat.oneDecimal, StatFormat.oneDecimal, StatFormat.oneDecimal, StatFormat.oneDecimal)))
            .setIndicators(
                    abilitySpeedIndicator,
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_defensive", 9, abilityDefensiveGetter,
                            new TooltipGetterMultiValue("tetra.stats.execute_defensive.tooltip",
                                    withStats(abilityDefensiveGetter, multiply(abilityDefensiveGetter, new StatGetterAbilityDamage(0, 0.01)),
                                            abilityDefEffGetter, multiply(abilityDefEffGetter, new StatGetterAbilityDamage(0, 0.01))),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.noDecimal, StatFormat.oneDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_overcharge", 10, abilityOverchargeGetter,
                            new TooltipGetterMultiValue("tetra.stats.execute_overcharge.tooltip",
                                    withStats(abilityOverchargeGetter), withFormat(StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_momentum", 11, abilityMomentumGetter,
                            new TooltipGetterMultiValue("tetra.stats.execute_momentum.tooltip",
                                    withStats(abilityMomentumGetter), withFormat(StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_combo", 12, abilityComboGetter,
                            new TooltipGetterMultiValue("tetra.stats.execute_combo.tooltip",
                                    withStats(abilityComboGetter), withFormat(StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.execute_revenge", 13, abilityRevengeGetter,
                            new TooltipGetterMultiValue("tetra.stats.execute_revenge.tooltip",
                                    withStats(abilityRevengeGetter), withFormat(StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_overextend", 14, abilityOverextendGetter,
                            new TooltipGetterMultiValue("tetra.stats.execute_overextend.tooltip",
                                    withStats(abilityOverextendGetter), withFormat(StatFormat.noDecimal))));

    public static final IStatGetter slamGetter = new StatGetterEffectLevel(ItemEffect.slam, 1);
    public static final IStatGetter slamEntityGetter = new StatGetterEffectLevel(ItemEffect.slam, 1.5);
    public static final GuiStatBar slam = new GuiStatBar(0, 0, barLength, "tetra.stats.slam",
            0, 200, false, slamGetter, LabelGetterBasic.percentageLabel,
            new TooltipGetterMultiValue("tetra.stats.slam.tooltip",
                    withStats(slamGetter, multiply(slamGetter, new StatGetterAbilityDamage(0, 0.01)),
                            slamEntityGetter, multiply(slamEntityGetter, new StatGetterAbilityDamage(0, 0.01)),
                            new StatGetterAbilityChargeTime(SlamEffect.instance), new StatGetterAbilityCooldown(SlamEffect.instance)),
                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.oneDecimal, StatFormat.oneDecimal)))
            .setIndicators(
                    abilitySpeedIndicator,
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_defensive", 9, abilityDefensiveGetter,
                            new TooltipGetterMultiValue("tetra.stats.slam_defensive.tooltip",
                                    withStats(new StatGetterAbilityDamage(0, 0.3),
                                            new StatGetterEffectLevel(ItemEffect.abilityDefensive, 0.05), abilityDefEffGetter),
                                    withFormat(StatFormat.oneDecimal, StatFormat.oneDecimal, StatFormat.oneDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_overcharge", 10, abilityOverchargeGetter,
                            new TooltipGetterMultiValue("tetra.stats.slam_overcharge.tooltip",
                                    withStats(abilityOverchargeGetter, multiply(abilityOverchargeGetter, new StatGetterAbilityDamage(0, 0.01)),
                                            abilityOverchargeEffGetter, abilityOverchargeGetter),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.oneDecimal, StatFormat.oneDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_momentum", 11, abilityMomentumGetter,
                            new TooltipGetterMultiValue("tetra.stats.slam_momentum.tooltip",
                                    withStats(new StatGetterEffectLevel(ItemEffect.abilityMomentum, 0.05),
                                            new StatGetterEffectLevel(ItemEffect.abilityMomentum, 0.03333)),
                                    withFormat(StatFormat.oneDecimal, StatFormat.oneDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_combo", 12, abilityComboGetter,
                            new TooltipGetterMultiValue("tetra.stats.slam_combo.tooltip",
                                    withStats(abilityComboGetter), withFormat(StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_revenge", 13, abilityRevengeGetter,
                            new TooltipGetterMultiValue("tetra.stats.slam_revenge.tooltip",
                                    withStats(new StatGetterEffectLevel(ItemEffect.abilityRevenge, 0.05)), withFormat(StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_overextend", 14, abilityOverextendGetter,
                            new TooltipGetterMultiValue("tetra.stats.slam_overextend.tooltip",
                                    withStats(abilityOverextendGetter, multiply(abilityOverextendGetter, new StatGetterAbilityDamage(0, 0.01)),
                                            abilityOverextendEffGetter, abilityOverextendGetter),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.oneDecimal, StatFormat.noDecimal))));

    public static final IStatGetter punctureGetter = new StatGetterEffectLevel(ItemEffect.puncture, 5);
    public static final GuiStatBar puncture = new GuiStatBar(0, 0, barLength, "tetra.stats.puncture",
            0, 200, false, punctureGetter, LabelGetterBasic.percentageLabel,
            new TooltipGetterMultiValue("tetra.stats.puncture.tooltip",
                    withStats(new StatGetterAbilityDamage(0, 1), punctureGetter, new StatGetterEffectEfficiency(ItemEffect.puncture, 1),
                            new StatGetterAbilityChargeTime(PunctureEffect.instance), new StatGetterAbilityCooldown(PunctureEffect.instance)),
                    withFormat(StatFormat.oneDecimal, StatFormat.noDecimal, StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.oneDecimal)))
            .setIndicators(
                    abilitySpeedIndicator,
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_defensive", 9, abilityDefensiveGetter,
                            new TooltipGetterMultiValue("tetra.stats.puncture_defensive.tooltip",
                                    withStats(new StatGetterAbilityDamage(0, 0.3),
                                            new StatGetterEffectLevel(ItemEffect.abilityDefensive, 15), abilityDefEffGetter),
                                    withFormat(StatFormat.oneDecimal, StatFormat.noDecimal, StatFormat.oneDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_overcharge", 10, abilityOverchargeGetter,
                            new TooltipGetterMultiValue("tetra.stats.puncture_overcharge.tooltip",
                                    withStats(new StatGetterEffectLevel(ItemEffect.abilityOvercharge, 5),
                                            new StatGetterEffectEfficiency(ItemEffect.abilityOvercharge, 0.5)),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_momentum", 11, abilityMomentumGetter,
                            new TooltipGetterNone("tetra.stats.puncture_momentum.tooltip")),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_combo", 12, abilityComboGetter,
                            new TooltipGetterMultiValue("tetra.stats.puncture_combo.tooltip",
                                    withStats(new StatGetterEffectLevel(ItemEffect.abilityCombo, 0.05)), withFormat(StatFormat.oneDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.puncture_revenge", 13, abilityRevengeGetter,
                            new TooltipGetterNone("tetra.stats.puncture_revenge.tooltip")),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_overextend", 14, abilityOverextendGetter,
                            new TooltipGetterMultiValue("tetra.stats.puncture_overextend.tooltip",
                                    withStats(new StatGetterEffectLevel(ItemEffect.abilityOverextend, 5), abilityOverextendEffGetter),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal))));

    public static final IStatGetter pryGetter = new StatGetterEffectLevel(ItemEffect.pry, 1);
    public static final GuiStatBar pry = new GuiStatBar(0, 0, barLength, "tetra.stats.pry_armor",
            0, 10, false, pryGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterMultiValue("tetra.stats.pry_armor.tooltip",
                    withStats(new StatGetterAbilityDamage(0, 0.5), pryGetter, new StatGetterEffectEfficiency(ItemEffect.pry, 1),
                            new StatGetterCooldown(PryEffect.flatCooldown, PryEffect.cooldownSpeedMultiplier)),
                    withFormat(StatFormat.oneDecimal, StatFormat.noDecimal, StatFormat.noDecimal, StatFormat.oneDecimal)))
            .setIndicators(
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_speed_bonus", 8,
                            abilitySpeedGetter, new TooltipGetterPercentage("tetra.stats.pry_speed_bonus.tooltip", abilitySpeedGetter)),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_defensive", 9, abilityDefensiveGetter,
                            new TooltipGetterMultiValue("tetra.stats.pry_defensive.tooltip",
                                    withStats(new StatGetterEffectLevel(ItemEffect.abilityDefensive, 4), abilityDefEffGetter),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_overcharge", 10, abilityOverchargeGetter,
                            new TooltipGetterMultiValue("tetra.stats.pry_overcharge.tooltip",
                                    withStats(abilityOverchargeGetter, multiply(abilityOverchargeGetter, new StatGetterAbilityDamage(0, 0.01)),
                                            abilityOverchargeEffGetter, new StatGetterAbilityChargeTime(PryChargedEffect.instance)),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.noDecimal, StatFormat.oneDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_momentum", 11, abilityMomentumGetter,
                            new TooltipGetterMultiValue("tetra.stats.pry_momentum.tooltip",
                                    withStats(new StatGetterEffectLevel(ItemEffect.abilityMomentum, 0.05)),
                                    withFormat(StatFormat.oneDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_combo", 12, abilityComboGetter,
                            new TooltipGetterMultiValue("tetra.stats.pry_combo.tooltip",
                                    withStats(abilityComboGetter, multiply(abilityComboGetter, new StatGetterAbilityDamage(0, 0.01)),
                                            abilityComboEffGetter),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_revenge", 13, abilityRevengeGetter,
                            new TooltipGetterMultiValue("tetra.stats.pry_revenge.tooltip",
                                    withStats(abilityRevengeGetter, multiply(abilityRevengeGetter, new StatGetterAbilityDamage(0, 0.01))),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_overextend", 14, abilityOverextendGetter,
                            new TooltipGetterNone("tetra.stats.pry_overextend.tooltip")));

    public static final IStatGetter overpowerGetter = new StatGetterEffectLevel(ItemEffect.overpower, 1);
    public static final GuiStatBar overpower = new GuiStatBar(0, 0, barLength, "tetra.stats.overpower",
            0, 300, false, overpowerGetter, LabelGetterBasic.percentageLabel,
            new TooltipGetterMultiValue("tetra.stats.overpower.tooltip",
                    withStats(overpowerGetter, multiply(overpowerGetter, new StatGetterAbilityDamage(0, 0.01)),
                            new StatGetterEffectEfficiency(ItemEffect.overpower, 1),
                            new StatGetterAbilityChargeTime(OverpowerEffect.instance), new StatGetterAbilityCooldown(OverpowerEffect.instance)),
                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.oneDecimal)))
            .setIndicators(
                    abilitySpeedIndicator,
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_defensive", 9, abilityDefensiveGetter,
                            new TooltipGetterMultiValue("tetra.stats.overpower_defensive.tooltip",
                                    withStats(abilityDefensiveGetter, multiply(abilityDefensiveGetter, new StatGetterAbilityDamage(0, 0.01)),
                                            abilityDefEffGetter),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_overcharge", 10, abilityOverchargeGetter,
                            new TooltipGetterMultiValue("tetra.stats.overpower_overcharge.tooltip",
                                    withStats(abilityOverchargeGetter, multiply(abilityOverchargeGetter, new StatGetterAbilityDamage(0, 0.01)),
                                            abilityOverchargeEffGetter),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_momentum", 11, abilityMomentumGetter,
                            new TooltipGetterNone("tetra.stats.overpower_momentum.tooltip")),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_combo", 12, abilityComboGetter,
                            new TooltipGetterMultiValue("tetra.stats.overpower_combo.tooltip",
                                    withStats(abilityComboGetter, multiply(abilityComboGetter, new StatGetterAbilityDamage(0, 0.01)),
                                            abilityComboEffGetter),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_revenge", 13, abilityRevengeGetter,
                            new TooltipGetterMultiValue("tetra.stats.overpower_revenge.tooltip",
                                    withStats(abilityRevengeGetter, multiply(abilityRevengeGetter, new StatGetterAbilityDamage(0, 0.01))),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_overextend", 14, abilityOverextendGetter,
                            new TooltipGetterNone("tetra.stats.overpower_overextend.tooltip")));

    public static final IStatGetter reapGetter = new StatGetterEffectLevel(ItemEffect.reap, 1);
    public static final GuiStatBar reap = new GuiStatBar(0, 0, barLength, "tetra.stats.reap",
            0, 200, false, reapGetter, LabelGetterBasic.percentageLabel,
            new TooltipGetterMultiValue("tetra.stats.reap.tooltip",
                    withStats(reapGetter, multiply(reapGetter, new StatGetterAbilityDamage(0, 0.01)),
                            new StatGetterEffectEfficiency(ItemEffect.reap, 1),
                            new StatGetterAbilityChargeTime(ReapEffect.instance), new StatGetterAbilityCooldown(ReapEffect.instance)),
                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.oneDecimal)))
            .setIndicators(
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_speed_bonus", 8,
                            abilitySpeedGetter, new TooltipGetterMultiValue("tetra.stats.reap_speed_bonus.tooltip",
                            withStats(abilitySpeedGetter, new StatGetterEffectEfficiency(ItemEffect.abilitySpeed, 1)),
                            withFormat(StatFormat.noDecimal, StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_defensive", 9, abilityDefensiveGetter,
                            new TooltipGetterMultiValue("tetra.stats.reap_defensive.tooltip",
                                    withStats(new StatGetterEffectLevel(ItemEffect.abilityDefensive, 0.05), new StatGetterEffectLevel(ItemEffect.abilityDefensive, 0.1),
                                            abilityDefEffGetter),
                                    withFormat(StatFormat.noDecimal, StatFormat.noDecimal, StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_overcharge", 10, abilityOverchargeGetter,
                            new TooltipGetterMultiValue("tetra.stats.reap_overcharge.tooltip",
                                    withStats(abilityOverchargeGetter, multiply(abilityOverchargeGetter, new StatGetterAbilityDamage(0, 0.01)),
                                            abilityOverchargeEffGetter),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_momentum", 11, abilityMomentumGetter,
                            new TooltipGetterMultiValue("tetra.stats.reap_momentum.tooltip",
                                    withStats(abilityMomentumEffGetter, abilityMomentumGetter),
                                    withFormat(StatFormat.oneDecimal, StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_combo", 12, abilityComboGetter,
                            new TooltipGetterMultiValue("tetra.stats.reap_combo.tooltip",
                                    withStats(abilityComboGetter, multiply(abilityComboGetter, new StatGetterAbilityDamage(0, 0.01)),
                                            abilityComboEffGetter),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_revenge", 13, abilityRevengeGetter,
                            new TooltipGetterMultiValue("tetra.stats.reap_revenge.tooltip",
                                    withStats(abilityRevengeGetter, multiply(abilityRevengeGetter, new StatGetterAbilityDamage(0, 0.01)),
                                            abilityRevengeEffGetter),
                                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal, StatFormat.noDecimal))),
                    new GuiStatIndicator(0, 0, "tetra.stats.ability_overextend", 14, abilityOverextendGetter,
                            new TooltipGetterMultiValue("tetra.stats.reap_overextend.tooltip",
                                    withStats(abilityOverextendGetter), withFormat(StatFormat.noDecimal))));
}
