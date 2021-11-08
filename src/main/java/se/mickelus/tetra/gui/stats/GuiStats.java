package se.mickelus.tetra.gui.stats;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import se.mickelus.tetra.effect.*;
import se.mickelus.tetra.gui.stats.bar.GuiStatBar;
import se.mickelus.tetra.gui.stats.bar.GuiStatBarBlockingDuration;
import se.mickelus.tetra.gui.stats.bar.GuiStatBarIntegrity;
import se.mickelus.tetra.gui.stats.bar.GuiStatIndicator;
import se.mickelus.tetra.gui.stats.getter.*;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.PotionsInventory;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.QuickslotInventory;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.QuiverInventory;
import se.mickelus.tetra.items.modular.impl.toolbelt.inventory.StorageInventory;
import se.mickelus.tetra.properties.TetraAttributes;

import static se.mickelus.tetra.gui.stats.StatsHelper.*;

public class GuiStats {
    public static final IStatGetter attackDamageGetter = new StatGetterAttribute(Attributes.ATTACK_DAMAGE);
    public static final GuiStatBar attackDamage = new GuiStatBar(0, 0, barLength, "tetra.stats.attack_damage",
                0, 40, false, attackDamageGetter, LabelGetterBasic.decimalLabel,
            new TooltipGetterDecimal("tetra.stats.attack_damage.tooltip", attackDamageGetter));

    public static final IStatGetter attackDamageNormalizedGetter = new StatGetterAttribute(Attributes.ATTACK_DAMAGE, true);
    public static final GuiStatBar attackDamageNormalized = new GuiStatBar(0, 0, barLength, "tetra.stats.attack_damage_normalized",
            0, 20, false, attackDamageNormalizedGetter, LabelGetterBasic.decimalLabel,
            new TooltipGetterDecimal("tetra.stats.attack_damage_normalized.tooltip", attackDamageNormalizedGetter));

    public static final IStatGetter counterweightGetter = new StatGetterEffectLevel(ItemEffect.counterweight, 1);
    private static final ITooltipGetter counterweightTooltip = new TooltipGetterInteger("tetra.stats.counterweight.tooltip", counterweightGetter);
    public static final GuiStatBar counterweight = new GuiStatBar(0, 0, barLength, "tetra.stats.counterweight",
            0, 12, true, counterweightGetter, LabelGetterBasic.integerLabel, counterweightTooltip);

    public static final IStatGetter attackSpeedGetter = new StatGetterAttribute(Attributes.ATTACK_SPEED);
    public static final GuiStatBar attackSpeed = new GuiStatBar(0, 0, barLength, "tetra.stats.speed",
                0, 4, false, attackSpeedGetter, LabelGetterBasic.decimalLabel, new TooltipGetterAttackSpeed(attackSpeedGetter))
            .setIndicators(new GuiStatIndicator(0, 0, "tetra.stats.counterweight", 5, counterweightGetter, new TooltipGetterCounterweight()));

    public static final IStatGetter attackSpeedGetterNormalized = new StatGetterAttribute(Attributes.ATTACK_SPEED, true, true);
    public static final GuiStatBar attackSpeedNormalized = new GuiStatBar(0, 0, barLength, "tetra.stats.speed_normalized",
            -3, 3, false, true, false, attackSpeedGetterNormalized, LabelGetterBasic.decimalLabel,
            new TooltipGetterDecimal("tetra.stats.speed_normalized.tooltip", attackSpeedGetterNormalized));

    public static final IStatGetter drawStrengthGetter = new StatGetterAttribute(TetraAttributes.drawStrength.get());
    public static final GuiStatBar drawStrength = new GuiStatBar(0, 0, barLength, "tetra.stats.draw_strength",
            0, 40, false, drawStrengthGetter, LabelGetterBasic.singleDecimalLabel,
            new TooltipGetterDrawStrength(drawStrengthGetter));

    public static final IStatGetter drawSpeedGetter = new StatGetterAttribute(TetraAttributes.drawSpeed.get());
    public static final GuiStatBar drawSpeed = new GuiStatBar(0, 0, barLength, "tetra.stats.draw_speed",
            0, 10, false, false, true, drawSpeedGetter, LabelGetterBasic.decimalLabelInverted,
            new TooltipGetterDecimal("tetra.stats.draw_speed.tooltip", drawSpeedGetter));

    public static final GuiStatBar drawSpeedNormalized = new GuiStatBar(0, 0, barLength, "tetra.stats.draw_speed_normalized",
            -4, 4, false, true, true,
            drawSpeedGetter, LabelGetterBasic.decimalLabelInverted,
            new TooltipGetterDecimal("tetra.stats.draw_speed_normalized.tooltip", drawSpeedGetter));

    public static final IStatGetter abilityDamageGetter = new StatGetterAttribute(TetraAttributes.abilityDamage.get());
    public static final GuiStatBar abilityDamage = new GuiStatBar(0, 0, barLength, "tetra.stats.ability_damage",
            0, 40, false, abilityDamageGetter, LabelGetterBasic.decimalLabel,
            new TooltipGetterDecimal("tetra.stats.ability_damage.tooltip", abilityDamageGetter));

    public static final IStatGetter abilityCooldownGetter = new StatGetterAttribute(TetraAttributes.abilityCooldown.get());
    public static final GuiStatBar abilityCooldown = new GuiStatBar(0, 0, barLength, "tetra.stats.ability_speed",
            0, 32, false, false, true, abilityCooldownGetter, LabelGetterBasic.decimalLabelInverted,
            new TooltipGetterDecimal("tetra.stats.ability_speed.tooltip", abilityCooldownGetter));

    public static final GuiStatBar abilityCooldownNormalized = new GuiStatBar(0, 0, barLength, "tetra.stats.ability_speed_normalized",
            -16, 16, false, true, true,
            abilityCooldownGetter, LabelGetterBasic.decimalLabelInverted,
            new TooltipGetterDecimal("tetra.stats.ability_speed_normalized.tooltip", abilityCooldownGetter));

    public static final IStatGetter reachGetter = new StatGetterAttribute(ForgeMod.REACH_DISTANCE.get()).withOffset(-0.5);
    public static final GuiStatBar reach = new GuiStatBar(0, 0, barLength, "tetra.stats.reach",
            0, 20, false, reachGetter, LabelGetterBasic.decimalLabel,
            new TooltipGetterReach(reachGetter));

    public static final IStatGetter reachGetterNormalized = new StatGetterAttribute(ForgeMod.REACH_DISTANCE.get(), true);
    public static final GuiStatBar reachNormalized = new GuiStatBar(0, 0, barLength, "tetra.stats.reach_normalized",
            0, 20, false, true, false, reachGetterNormalized, LabelGetterBasic.decimalLabel,
            new TooltipGetterDecimal("tetra.stats.reach_normalized.tooltip", reachGetterNormalized));

    public static final IStatGetter durabilityGetter = new StatGetterDurability();
    public static final GuiStatBar durability = new GuiStatBar(0, 0, barLength, "tetra.stats.durability",
                0, 2400, false, durabilityGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.durability.tooltip", durabilityGetter));

    public static final IStatGetter armorGetter = new StatGetterAttribute(Attributes.ARMOR);
    public static final GuiStatBar armor = new GuiStatBar(0, 0, barLength, "tetra.stats.armor",
                0, 20, false, armorGetter, LabelGetterBasic.singleDecimalLabel,
            new TooltipGetterInteger("tetra.stats.armor.tooltip", armorGetter));

    public static final IStatGetter toughnessGetter = new StatGetterAttribute(Attributes.ARMOR_TOUGHNESS);
    public static final GuiStatBar toughness = new GuiStatBar(0, 0, barLength, "tetra.stats.toughness",
            0, 20, false, toughnessGetter, LabelGetterBasic.singleDecimalLabel,
            new TooltipGetterInteger("tetra.stats.toughness.tooltip", toughnessGetter));

    public static final IStatGetter shieldbreakerGetter = new StatGetterEffectLevel(ItemEffect.shieldbreaker, 1d);
    public static final GuiStatBar shieldbreaker = new GuiStatBar(0, 0, barLength, "tetra.stats.shieldbreaker",
            0, 1, false, shieldbreakerGetter, LabelGetterBasic.noLabel,
            new TooltipGetterNone("tetra.stats.shieldbreaker.tooltip"));

    public static final GuiStatBar blocking = new GuiStatBarBlockingDuration(0, 0, barLength);

    public static final IStatGetter blockingReflectGetter = new StatGetterEffectLevel(ItemEffect.blockingReflect, 1d);
    public static final GuiStatBar blockingReflect = new GuiStatBar(0, 0, barLength, "tetra.stats.blocking_reflect",
            0, 100, false, blockingReflectGetter, LabelGetterBasic.percentageLabel,
            new TooltipGetterBlockingReflect());

    public static final IStatGetter bashingGetter = new StatGetterEffectLevel(ItemEffect.bashing, 1d);
    public static final GuiStatBar bashing = new GuiStatBar(0, 0, barLength, "tetra.stats.bashing",
            0, 16, false, bashingGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterBashing());

    public static final IStatGetter throwableGetter = new StatGetterEffectEfficiency(ItemEffect.throwable, 100d);
    public static final GuiStatBar throwable = new GuiStatBar(0, 0, barLength, "tetra.stats.throwable",
            0, 300, false, throwableGetter, LabelGetterBasic.percentageLabel,
            new TooltipGetterPercentageDecimal("tetra.stats.throwable.tooltip", throwableGetter));

    public static final IStatGetter ricochetGetter = new StatGetterEffectLevel(ItemEffect.ricochet, 1);
    public static final GuiStatBar ricochet = new GuiStatBar(0, 0, barLength, "tetra.stats.ricochet",
            0, 12, true, ricochetGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.ricochet.tooltip", ricochetGetter));

    public static final IStatGetter piercingGetter = new StatGetterEffectLevel(ItemEffect.piercing, 1);
    public static final GuiStatBar piercing = new GuiStatBar(0, 0, barLength, "tetra.stats.piercing",
            0, 12, true, piercingGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.piercing.tooltip", piercingGetter))
            .setIndicators(new GuiStatIndicator(0, 0, "tetra.stats.piercing_harvest", 6,
                    new StatGetterEffectLevel(ItemEffect.piercingHarvest, 1), new TooltipGetterNone("tetra.stats.piercing_harvest.tooltip")));

    public static final IStatGetter jabGetter = new StatGetterEffectLevel(ItemEffect.jab, 1);
    public static final GuiStatBar jab = new GuiStatBar(0, 0, barLength, "tetra.stats.jab",
            0, 300, false, jabGetter, LabelGetterBasic.percentageLabel,
            new TooltipGetterPercentageDecimal("tetra.stats.jab.tooltip", jabGetter));

    public static final IStatGetter quickslotGetter = new StatGetterEffectLevel(ItemEffect.quickSlot, 1d);
    public static final GuiStatBar quickslot = new GuiStatBar(0, 0, barLength, "tetra.stats.toolbelt.quickslot",
                0, QuickslotInventory.maxSize, true, quickslotGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.toolbelt.quickslot.tooltip", quickslotGetter));

    public static final IStatGetter potionStorageGetter = new StatGetterEffectLevel(ItemEffect.potionSlot, 1d);
    public static final GuiStatBar potionStorage = new GuiStatBar(0, 0, barLength, "tetra.stats.toolbelt.potion_storage",
                0, PotionsInventory.maxSize, true, potionStorageGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.toolbelt.potion_storage.tooltip", potionStorageGetter));

    public static final IStatGetter storageGetter = new StatGetterEffectLevel(ItemEffect.storageSlot, 1d);
    public static final GuiStatBar storage = new GuiStatBar(0, 0, barLength, "tetra.stats.toolbelt.storage",
                0, StorageInventory.maxSize, false, storageGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.toolbelt.storage.tooltip", storageGetter));

    public static final IStatGetter quiverGetter = new StatGetterEffectLevel(ItemEffect.quiverSlot, 1d);
    public static final GuiStatBar quiver = new GuiStatBar(0, 0, barLength, "tetra.stats.toolbelt.quiver",
                0, QuiverInventory.maxSize, true, quiverGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.toolbelt.quiver.tooltip", quiverGetter));

    public static final IStatGetter boosterGetter = new StatGetterEffectLevel(ItemEffect.booster, 1d);
    public static final GuiStatBar booster = new GuiStatBar(0, 0, barLength, "tetra.stats.toolbelt.booster",
                0, 3, true, boosterGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.toolbelt.booster.tooltip", boosterGetter));

    public static final IStatGetter suspendSelfGetter = new StatGetterEffectLevel(ItemEffect.suspendSelf, 1d);
    public static final GuiStatBar suspendSelf = new GuiStatBar(0, 0, barLength, "tetra.stats.toolbelt.suspend_self",
            0, 1, false, suspendSelfGetter, LabelGetterBasic.noLabel,
            new TooltipGetterNone("tetra.stats.toolbelt.suspend_self.tooltip"));

    public static final IStatGetter sweepingGetter = new StatGetterEffectLevel(ItemEffect.sweeping, 12.5);
    public static final GuiStatBar sweeping = new GuiStatBar(0, 0, barLength, "tetra.stats.sweeping",
                0, 100, false, sweepingGetter, LabelGetterBasic.percentageLabelDecimal,
            new TooltipGetterSweeping(sweepingGetter))
            .setIndicators(new GuiStatIndicator(0, 0, "tetra.stats.truesweep", 4,
                    new StatGetterEffectLevel(ItemEffect.truesweep, 1), new TooltipGetterNone("tetra.stats.truesweep.tooltip")));

    public static final IStatGetter bleedingGetter = new StatGetterEffectLevel(ItemEffect.bleeding, 4);
    public static final GuiStatBar bleeding = new GuiStatBar(0, 0, barLength, "tetra.stats.bleeding",
                0, 20, false, bleedingGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.bleeding.tooltip", bleedingGetter));

    public static final IStatGetter backstabGetter = new StatGetterEffectLevel(ItemEffect.backstab, 25, 25);
    public static final GuiStatBar backstab = new GuiStatBar(0, 0, barLength, "tetra.stats.backstab",
                0, 200, false, backstabGetter, LabelGetterBasic.percentageLabelDecimal,
            new TooltipGetterPercentageDecimal("tetra.stats.backstab.tooltip", backstabGetter));

    public static final IStatGetter armorPenetrationGetter = new StatGetterEffectLevel(ItemEffect.armorPenetration, 1);
    public static final GuiStatBar armorPenetration = new GuiStatBar(0, 0, barLength, "tetra.stats.armorPenetration",
                0, 100, false, armorPenetrationGetter, LabelGetterBasic.percentageLabel,
            new TooltipGetterPercentage("tetra.stats.armorPenetration.tooltip", armorPenetrationGetter));

    public static final IStatGetter crushingGetter = new StatGetterEffectLevel(ItemEffect.crushing, 1);
    public static final GuiStatBar crushing = new GuiStatBar(0, 0, barLength, "tetra.stats.crushing",
            0, 10, false, crushingGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.crushing.tooltip", crushingGetter));

    public static final IStatGetter skeweringGetter = new StatGetterEffectLevel(ItemEffect.skewering, 1);
    public static final GuiStatBar skewering = new GuiStatBar(0, 0, barLength, "tetra.stats.skewering",
                0, 10, false, skeweringGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterMultiValue("tetra.stats.skewering.tooltip",
                    withStats(skeweringGetter, new StatGetterEffectEfficiency(ItemEffect.skewering, 1)),
                    withFormat(StatFormat.noDecimal, StatFormat.noDecimal)));

    public static final IStatGetter severingGetter = new StatGetterEffectLevel(ItemEffect.severing, 1);
    public static final GuiStatBar severing = new GuiStatBar(0, 0, barLength, "tetra.stats.severing",
            0, 100, false, severingGetter, LabelGetterBasic.percentageLabel,
            new TooltipGetterMultiValue("tetra.stats.severing.tooltip",
                    withStats(severingGetter, new StatGetterEffectEfficiency(ItemEffect.severing, 1)),
                    withFormat(StatFormat.noDecimal, StatFormat.noDecimal)));

    public static final IStatGetter stunGetter = new StatGetterEffectLevel(ItemEffect.stun, 1);
    public static final GuiStatBar stun = new GuiStatBar(0, 0, barLength, "tetra.stats.stun",
            0, 100, false, stunGetter, LabelGetterBasic.percentageLabel,
            new TooltipGetterMultiValue("tetra.stats.stun.tooltip",
                    withStats(stunGetter, new StatGetterEffectEfficiency(ItemEffect.stun, 1)),
                    withFormat(StatFormat.noDecimal, StatFormat.oneDecimal)));

    public static final IStatGetter howlingGetter = new StatGetterEffectLevel(ItemEffect.howling, 1);
    public static final GuiStatBar howling = new GuiStatBar(0, 0, barLength, "tetra.stats.howling",
            0, 8, false, howlingGetter, LabelGetterBasic.integerLabel, new TooltipGetterHowling());

    public static final IStatGetter knockbackGetter = new StatGetterEnchantmentLevel(Enchantments.KNOCKBACK, 0.5);
    public static final GuiStatBar knockback = new GuiStatBar(0, 0, barLength, "tetra.stats.knockback",
                0, 10, false, knockbackGetter, LabelGetterBasic.decimalLabel,
            new TooltipGetterDecimal("tetra.stats.knockback.tooltip", knockbackGetter));

    public static final IStatGetter lootingGetter = new StatGetterEnchantmentLevel(Enchantments.LOOTING, 1);
    public static final GuiStatBar looting = new GuiStatBar(0, 0, barLength, "tetra.stats.looting",
                0, 20, false, lootingGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.looting.tooltip", lootingGetter));

    public static final IStatGetter fieryGetter = new StatGetterEnchantmentLevel(Enchantments.FIRE_ASPECT, 4);
    public static final GuiStatBar fiery = new GuiStatBar(0, 0, barLength, "tetra.stats.fiery",
                0, 32, false, fieryGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.fiery.tooltip", fieryGetter));

    public static final IStatGetter smiteGetter = new StatGetterEnchantmentLevel(Enchantments.SMITE, 2.5);
    public static final GuiStatBar smite = new GuiStatBar(0, 0, barLength, "tetra.stats.smite",
                0, 25, false, smiteGetter, LabelGetterBasic.decimalLabel,
            new TooltipGetterDecimal("tetra.stats.smite.tooltip", smiteGetter));

    public static final IStatGetter arthropodGetter = new StatGetterEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS, 2.5);
    public static final GuiStatBar arthropod = new GuiStatBar(0, 0, barLength, "tetra.stats.arthropod",
                0, 25, false, arthropodGetter, LabelGetterBasic.decimalLabel,
            new TooltipGetterArthropod());

    public static final IStatGetter unbreakingGetter = new StatGetterEffectLevel(ItemEffect.unbreaking, 1);
    public static final GuiStatBar unbreaking = new GuiStatBar(0, 0, barLength, "tetra.stats.unbreaking",
                0, 20, true, unbreakingGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterUnbreaking());

    public static final IStatGetter mendingGetter = new StatGetterEnchantmentLevel(Enchantments.MENDING, 2);
    public static final GuiStatBar mending = new GuiStatBar(0, 0, barLength, "tetra.stats.mending",
                0, 10, false, mendingGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.mending.tooltip", mendingGetter));

    public static final IStatGetter silkTouchGetter = new StatGetterEnchantmentLevel(Enchantments.SILK_TOUCH, 1);
    public static final GuiStatBar silkTouch = new GuiStatBar(0, 0, barLength, "tetra.stats.silkTouch",
                0, 1, false, silkTouchGetter, LabelGetterBasic.noLabel,
            new TooltipGetterDecimal("tetra.stats.silkTouch.tooltip", silkTouchGetter));

    public static final IStatGetter fortuneGetter = new StatGetterEnchantmentLevel(Enchantments.FORTUNE, 1);
    public static final GuiStatBar fortune = new GuiStatBar(0, 0, barLength, "tetra.stats.fortune",
                0, 20, false, fortuneGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.fortune.tooltip", fortuneGetter));

    public static final IStatGetter infinityGetter = new StatGetterEnchantmentLevel(Enchantments.INFINITY, 1);
    public static final GuiStatBar infinity = new GuiStatBar(0, 0, barLength, "tetra.stats.infinity",
            0, 1, false, infinityGetter, LabelGetterBasic.noLabel,
            new TooltipGetterInteger("tetra.stats.infinity.tooltip", infinityGetter));

    public static final IStatGetter flameGetter = new StatGetterEnchantmentLevel(Enchantments.FLAME, 4);
    public static final GuiStatBar flame = new GuiStatBar(0, 0, barLength, "tetra.stats.flame",
            0, 2, false, flameGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.flame.tooltip", flameGetter));

    public static final IStatGetter punchGetter = new StatGetterEnchantmentLevel(Enchantments.PUNCH, 1);
    public static final GuiStatBar punch = new GuiStatBar(0, 0, barLength, "tetra.stats.punch",
            0, 4, false, punchGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.punch.tooltip", punchGetter));

    public static final IStatGetter quickStrikeGetter = new StatGetterEffectLevel(ItemEffect.quickStrike, 5, 20);
    public static final GuiStatBar quickStrike = new GuiStatBar(0, 0, barLength, "tetra.stats.quickStrike",
                0, 100, false, quickStrikeGetter, LabelGetterBasic.percentageLabelDecimal,
            new TooltipGetterPercentageDecimal("tetra.stats.quickStrike.tooltip", quickStrikeGetter));

    public static final IStatGetter softStrikeGetter = new StatGetterEffectLevel(ItemEffect.softStrike, 1);
    public static final GuiStatBar softStrike = new GuiStatBar(0, 0, barLength, "tetra.stats.softStrike",
                0, 1, false, softStrikeGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.softStrike.tooltip", softStrikeGetter));

    public static final IStatGetter fierySelfGetter = new StatGetterEffectEfficiency(ItemEffect.fierySelf, 100);
    public static final GuiStatBar fierySelf = new GuiStatBar(0, 0, barLength, "tetra.stats.fierySelf",
                0, 100, false, false, true, fierySelfGetter, LabelGetterBasic.percentageLabelDecimalInverted,
            new TooltipGetterFierySelf());

    public static final IStatGetter enderReverbGetter = new StatGetterEffectEfficiency(ItemEffect.enderReverb, 100);
    public static final GuiStatBar enderReverb = new GuiStatBar(0, 0, barLength, "tetra.stats.enderReverb",
                0, 100, false, false, true, enderReverbGetter, LabelGetterBasic.percentageLabelDecimalInverted,
            new TooltipGetterPercentageDecimal("tetra.stats.enderReverb.tooltip", enderReverbGetter));

    public static final IStatGetter criticalGetter = new StatGetterEffectLevel(ItemEffect.criticalStrike, 1);
    public static final GuiStatBar criticalStrike = new GuiStatBar(0, 0, barLength, "tetra.stats.criticalStrike",
            0, 100, false, criticalGetter, LabelGetterBasic.percentageLabel,
            new TooltipGetterCriticalStrike());

    public static final IStatGetter intuitGetter = new StatGetterEffectLevel(ItemEffect.intuit, 1);
    public static final GuiStatBar intuit = new GuiStatBar(0, 0, barLength, "tetra.stats.intuit",
            0, 8, false, intuitGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.intuit.tooltip", intuitGetter));

    public static final IStatGetter earthbindGetter = new StatGetterEffectLevel(ItemEffect.earthbind, 1);
    public static final GuiStatBar earthbind = new GuiStatBar(0, 0, barLength, "tetra.stats.earthbind",
            0, 16, false, earthbindGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.earthbind.tooltip", earthbindGetter));

    public static final IStatGetter releaseLatchGetter = new StatGetterEffectLevel(ItemEffect.releaseLatch, 1);
    public static final GuiStatBar releaseLatch = new GuiStatBar(0, 0, barLength, "tetra.stats.bow.releaseLatch",
            0, 1, false, releaseLatchGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.bow.releaseLatch.tooltip", releaseLatchGetter));

    public static final IStatGetter overbowedGetter = new StatGetterEffectLevel(ItemEffect.overbowed, 0.1);
    public static final GuiStatBar overbowed = new GuiStatBar(0, 0, barLength, "tetra.stats.bow.overbowed",
            0, 10, false, overbowedGetter, LabelGetterBasic.singleDecimalLabel,
            new TooltipGetterDecimalSingle("tetra.stats.bow.overbowed.tooltip", overbowedGetter));

    public static final IStatGetter multishotGetter = new StatGetterEffectLevel(ItemEffect.multishot, 1);
    public static final GuiStatBar multishot = new GuiStatBar(0, 0, barLength, "tetra.stats.multishot",
            0, 12, true, multishotGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterMultishot());

    public static final IStatGetter zoomGetter = new StatGetterEffectLevel(ItemEffect.zoom, 0.1);
    public static final GuiStatBar zoom = new GuiStatBar(0, 0, barLength, "tetra.stats.zoom",
            0, 10, false, zoomGetter, LabelGetterBasic.singleDecimalLabel,
            new TooltipGetterDecimalSingle("tetra.stats.zoom.tooltip", zoomGetter));

    public static final IStatGetter velocityGetter = new StatGetterEffectLevel(ItemEffect.velocity, 1);
    public static final IStatGetter suspendGetter = new StatGetterEffectLevel(ItemEffect.suspend, 1);
    public static final GuiStatBar velocity = new GuiStatBar(0, 0, barLength, "tetra.stats.velocity",
            0, 200, false, velocityGetter, LabelGetterBasic.percentageLabel, new TooltipGetterVelocity())
            .setIndicators(new GuiStatIndicator(0, 0, "tetra.stats.suspend", 3, suspendGetter, new TooltipGetterNone("tetra.stats.suspend.tooltip")));

    public static final IStatGetter magicCapacityGetter = new StatGetterMagicCapacity();
    public static final GuiStatBar magicCapacity = new GuiStatBar(0, 0, barLength, "tetra.stats.magicCapacity",
            0, 150, false, magicCapacityGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.magicCapacity.tooltip", magicCapacityGetter));

    public static final IStatGetter stabilityGetter = new StatGetterStability();
    public static final GuiStatBar stability = new GuiStatBar(0, 0, barLength, "tetra.stats.stability",
            -100, 100, false, true, false, stabilityGetter, LabelGetterBasic.percentageLabel,
            new TooltipGetterPercentageDecimal("tetra.stats.stability.tooltip", stabilityGetter));

    public static final IStatGetter workableGetter = new StatGetterEffectLevel(ItemEffect.workable, 1);
    public static final GuiStatBar workable = new GuiStatBar(0, 0, barLength, "tetra.stats.workable",
            0, 100, false, workableGetter, LabelGetterBasic.percentageLabel,
            new TooltipGetterPercentageDecimal("tetra.stats.workable.tooltip", workableGetter));

    public static final IStatGetter scannerRangeGetter = new StatGetterEffectLevel(ItemEffect.scannerRange, 1);
    public static final GuiStatBar scannerRange = new GuiStatBar(0, 0, barLength, "tetra.stats.holo.scannerRange",
            0, 64, false, scannerRangeGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.holo.scannerRange.tooltip", scannerRangeGetter));

    public static final IStatGetter scannerHorizontalSpreadGetter = new StatGetterEffectLevel(ItemEffect.scannerHorizontalSpread, 4);
    public static final GuiStatBar scannerHorizontalSpread = new GuiStatBar(0, 0, barLength, "tetra.stats.holo.scannerHorizontalSpread",
            0, 128, false, scannerHorizontalSpreadGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterScannerHorizontalRange(scannerHorizontalSpreadGetter));

    public static final IStatGetter scannerVerticalSpreadGetter = new StatGetterEffectLevel(ItemEffect.scannerVerticalSpread, 10, 40);
    public static final GuiStatBar scannerVerticalSpread = new GuiStatBar(0, 0, barLength, "tetra.stats.holo.scannerVerticalSpread",
            0, 180, false, scannerVerticalSpreadGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("tetra.stats.holo.scannerVerticalSpread.tooltip", scannerVerticalSpreadGetter));

    public static final GuiStatBarIntegrity integrity = new GuiStatBarIntegrity(0, 0);


// todo: remaining effects
//        ItemEffect.strikingAxe
//        ItemEffect.strikingPickaxe
//        ItemEffect.strikingCut
//        ItemEffect.strikingShovel
//        ItemEffect.sweepingStrike
//        ItemEffect.flattening
//        ItemEffect.tilling
//        ItemEffect.denailing
}
