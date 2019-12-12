package se.mickelus.tetra.gui.statbar;

import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantments;
import se.mickelus.tetra.gui.statbar.getter.*;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryPotions;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryQuickslot;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryQuiver;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryStorage;
import se.mickelus.tetra.module.ItemEffect;

public class GuiStats {

    public static final int barLength = 59;

    public static final IStatGetter damageGetter = new StatGetterDamage();
    public static final GuiStatBar damage = new GuiStatBar(0, 0,barLength, I18n.format("stats.damage"),
                0, 40, false, damageGetter, LabelGetterBasic.decimalLabel,
            new TooltipGetterDecimal("stats.damage.tooltip", damageGetter));

    public static final IStatGetter speedGetter = new StatGetterSpeed();
    public static final GuiStatBar speed = new GuiStatBar(0, 0, barLength, I18n.format("stats.speed"),
                0, 4, false, speedGetter, LabelGetterBasic.decimalLabel,
            new TooltipGetterSpeed());

    public static final IStatGetter speedGetterNormalized = new StatGetterSpeedNormalized();
    public static final GuiStatBar speedNormalized = new GuiStatBar(0, 0, barLength, I18n.format("stats.speed_normalized"),
            -3, 3, false, true, speedGetterNormalized, LabelGetterBasic.decimalLabel,
            new TooltipGetterDecimal("stats.speed_normalized.tooltip", speedGetterNormalized));

    public static final IStatGetter durabilityGetter = new StatGetterDurability();
    public static final GuiStatBar durability = new GuiStatBar(0, 0, barLength, I18n.format("stats.durability"),
                0, 2400, false, durabilityGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.durability.tooltip", durabilityGetter));

    public static final IStatGetter armorGetter = new StatGetterEffectLevel(ItemEffect.armor, 1d);
    public static final GuiStatBar armor = new GuiStatBar(0, 0, barLength, I18n.format("stats.armor"),
                0, 20, false, armorGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.armor.tooltip", armorGetter));

    public static final IStatGetter quickslotGetter = new StatGetterEffectLevel(ItemEffect.quickSlot, 1d);
    public static final GuiStatBar quickslot = new GuiStatBar(0, 0, barLength, I18n.format("stats.toolbelt.quickslot"),
                0, InventoryQuickslot.maxSize, true, quickslotGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.toolbelt.quickslot.tooltip", quickslotGetter));

    public static final IStatGetter potionStorageGetter = new StatGetterEffectLevel(ItemEffect.potionSlot, 1d);
    public static final GuiStatBar potion_storage = new GuiStatBar(0, 0, barLength, I18n.format("stats.toolbelt.potion_storage"),
                0, InventoryPotions.maxSize, true, potionStorageGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.toolbelt.potion_storage.tooltip", potionStorageGetter));

    public static final IStatGetter storageGetter = new StatGetterEffectLevel(ItemEffect.storageSlot, 1d);
    public static final GuiStatBar storage = new GuiStatBar(0, 0, barLength, I18n.format("stats.toolbelt.storage"),
                0, InventoryStorage.maxSize, true, storageGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.toolbelt.storage.tooltip", storageGetter));

    public static final IStatGetter quiverGetter = new StatGetterEffectLevel(ItemEffect.quiverSlot, 1d);
    public static final GuiStatBar quiver = new GuiStatBar(0, 0, barLength, I18n.format("stats.toolbelt.quiver"),
                0, InventoryQuiver.maxSize, true, quiverGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.toolbelt.quiver.tooltip", quiverGetter));

    public static final IStatGetter boosterGetter = new StatGetterEffectLevel(ItemEffect.booster, 1d);
    public static final GuiStatBar booster = new GuiStatBar(0, 0, barLength, I18n.format("stats.toolbelt.booster"),
                0, 3, true, boosterGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.toolbelt.booster.tooltip", boosterGetter));

    public static final IStatGetter sweepingGetter = new StatGetterEffectLevel(ItemEffect.sweeping, 12.5);
    public static final GuiStatBar sweeping = new GuiStatBar(0, 0, barLength, I18n.format("stats.sweeping"),
                0, 100, false, sweepingGetter, LabelGetterBasic.percentageLabelDecimal,
            new TooltipGetterPercentage("stats.sweeping.tooltip", sweepingGetter));

    public static final IStatGetter bleedingGetter = new StatGetterEffectLevel(ItemEffect.bleeding, 4);
    public static final GuiStatBar bleeding = new GuiStatBar(0, 0, barLength, I18n.format("stats.bleeding"),
                0, 12, false, bleedingGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.bleeding.tooltip", bleedingGetter));

    public static final IStatGetter backstabGetter = new StatGetterEffectLevel(ItemEffect.backstab, 25, 25);
    public static final GuiStatBar backstab = new GuiStatBar(0, 0, barLength, I18n.format("stats.backstab"),
                0, 200, false, backstabGetter, LabelGetterBasic.percentageLabelDecimal,
            new TooltipGetterPercentage("stats.backstab.tooltip", backstabGetter));

    public static final IStatGetter armorPenetrationGetter = new StatGetterEffectLevel(ItemEffect.armorPenetration, 1);
    public static final GuiStatBar armorPenetration = new GuiStatBar(0, 0, barLength, I18n.format("stats.armorPenetration"),
                0, 10, false, armorPenetrationGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.armorPenetration.tooltip", armorPenetrationGetter));

    public static final IStatGetter unarmoredDamageGetter = new StatGetterEffectLevel(ItemEffect.unarmoredDamage, 1);
    public static final GuiStatBar unarmoredDamage = new GuiStatBar(0, 0, barLength, I18n.format("stats.unarmoredDamage"),
                0, 10, false, unarmoredDamageGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.unarmoredDamage.tooltip", unarmoredDamageGetter));

    public static final IStatGetter knockbackGetter = new StatGetterEnchantmentLevel(Enchantments.KNOCKBACK, 0.5);
    public static final GuiStatBar knockback = new GuiStatBar(0, 0, barLength, I18n.format("stats.knockback"),
                0, 10, false, knockbackGetter, LabelGetterBasic.decimalLabel,
            new TooltipGetterDecimal("stats.knockback.tooltip", knockbackGetter));

    public static final IStatGetter lootingGetter = new StatGetterEnchantmentLevel(Enchantments.LOOTING, 1);
    public static final GuiStatBar looting = new GuiStatBar(0, 0, barLength, I18n.format("stats.looting"),
                0, 20, false, lootingGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.looting.tooltip", lootingGetter));

    public static final IStatGetter fieryGetter = new StatGetterEnchantmentLevel(Enchantments.FIRE_ASPECT, 4);
    public static final GuiStatBar fiery = new GuiStatBar(0, 0, barLength, I18n.format("stats.fiery"),
                0, 32, false, fieryGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.fiery.tooltip", fieryGetter));

    public static final IStatGetter smiteGetter = new StatGetterEnchantmentLevel(Enchantments.SMITE, 2.5);
    public static final GuiStatBar smite = new GuiStatBar(0, 0, barLength, I18n.format("stats.smite"),
                0, 25, false, smiteGetter, LabelGetterBasic.decimalLabel,
            new TooltipGetterDecimal("stats.smite.tooltip", smiteGetter));

    public static final IStatGetter arthropodGetter = new StatGetterEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS, 2.5);
    public static final GuiStatBar arthropod = new GuiStatBar(0, 0, barLength, I18n.format("stats.arthropod"),
                0, 25, false, arthropodGetter, LabelGetterBasic.decimalLabel,
            new TooltipGetterArthropod());

    public static final IStatGetter unbreakingGetter = new StatGetterEffectLevel(ItemEffect.unbreaking, 1);
    public static final GuiStatBar unbreaking = new GuiStatBar(0, 0, barLength, I18n.format("stats.unbreaking"),
                0, 20, true, unbreakingGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterUnbreaking());

    public static final IStatGetter mendingGetter = new StatGetterEnchantmentLevel(Enchantments.MENDING, 2);
    public static final GuiStatBar mending = new GuiStatBar(0, 0, barLength, I18n.format("stats.mending"),
                0, 10, false, mendingGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.mending.tooltip", mendingGetter));

    public static final IStatGetter silkTouchGetter = new StatGetterEnchantmentLevel(Enchantments.SILK_TOUCH, 1);
    public static final GuiStatBar silkTouch = new GuiStatBar(0, 0, barLength, I18n.format("stats.silkTouch"),
                0, 1, false, silkTouchGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterDecimal("stats.silkTouch.tooltip", silkTouchGetter));

    public static final IStatGetter fortuneGetter = new StatGetterEnchantmentLevel(Enchantments.FORTUNE, 1);
    public static final GuiStatBar fortune = new GuiStatBar(0, 0, barLength, I18n.format("stats.fortune"),
                0, 20, false, fortuneGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.fortune.tooltip", fortuneGetter));

    public static final IStatGetter quickStrikeGetter = new StatGetterEffectLevel(ItemEffect.quickStrike, 5, 20);
    public static final GuiStatBar quickStrike = new GuiStatBar(0, 0, barLength, I18n.format("stats.quickStrike"),
                0, 100, false, quickStrikeGetter, LabelGetterBasic.percentageLabelDecimal,
            new TooltipGetterPercentage("stats.quickStrike.tooltip", quickStrikeGetter));

    public static final IStatGetter softStrikeGetter = new StatGetterEffectLevel(ItemEffect.softStrike, 1);
    public static final GuiStatBar softStrike = new GuiStatBar(0, 0, barLength, I18n.format("stats.softStrike"),
                0, 1, false, softStrikeGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.softStrike.tooltip", softStrikeGetter));

    public static final IStatGetter fierySelfGetter = new StatGetterEffectEfficiency(ItemEffect.fierySelf, 100);
    public static final GuiStatBar fierySelf = new GuiStatBar(0, 0, barLength, I18n.format("stats.fierySelf"),
                0, 100, false, fierySelfGetter, LabelGetterBasic.percentageLabelDecimal,
            new TooltipGetterFierySelf());

    public static final IStatGetter enderReverbGetter = new StatGetterEffectEfficiency(ItemEffect.enderReverb, 100);
    public static final GuiStatBar enderReverb = new GuiStatBar(0, 0, barLength, I18n.format("stats.enderReverb"),
                0, 100, false, enderReverbGetter, LabelGetterBasic.percentageLabelDecimal,
            new TooltipGetterPercentage("stats.enderReverb.tooltip", enderReverbGetter));

    public static final IStatGetter criticalGetter = new StatGetterEffectLevel(ItemEffect.criticalStrike, 1);
    public static final GuiStatBar criticalStrike = new GuiStatBar(0, 0, barLength, I18n.format("stats.criticalStrike"),
            0, 100, false, criticalGetter, LabelGetterBasic.percentageLabel,
            new TooltipGetterCriticalStrike());

    public static final IStatGetter intuitGetter = new StatGetterEffectLevel(ItemEffect.intuit, 1);
    public static final GuiStatBar intuit = new GuiStatBar(0, 0, barLength, I18n.format("stats.intuit"),
            0, 8, false, intuitGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.intuit.tooltip", intuitGetter));

    public static final IStatGetter earthbindGetter = new StatGetterEffectLevel(ItemEffect.earthbind, 1);
    public static final GuiStatBar earthbind = new GuiStatBar(0, 0, barLength, I18n.format("stats.earthbind"),
            0, 100, false, earthbindGetter, LabelGetterBasic.percentageLabel,
            new TooltipGetterPercentage("stats.earthbind.tooltip", earthbindGetter));

    public static final IStatGetter magicCapacityGetter = new StatGetterMagicCapacity();
    public static final GuiStatBar magicCapacity = new GuiStatBar(0, 0, barLength, I18n.format("stats.magicCapacity"),
            0, 50, false, magicCapacityGetter, LabelGetterBasic.integerLabel,
            new TooltipGetterInteger("stats.magicCapacity.tooltip", magicCapacityGetter));

    public static final GuiStatBarIntegrity integrity = new GuiStatBarIntegrity(0, 0);



// todo: remaining effects
//        ItemEffect.strikingAxe
//        ItemEffect.strikingPickaxe
//        ItemEffect.strikingCut
//        ItemEffect.strikingShovel
//        ItemEffect.sweepingStrike
//        ItemEffect.flattening
//        ItemEffect.tilling
//        ItemEffect.counterweight
//        ItemEffect.denailing
}
