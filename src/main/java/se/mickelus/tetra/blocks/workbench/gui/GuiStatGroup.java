package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.gui.statbar.GuiStatBar;
import se.mickelus.tetra.gui.statbar.GuiStatBarCapability;
import se.mickelus.tetra.gui.statbar.GuiStatBase;
import se.mickelus.tetra.gui.statbar.getter.*;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryPotions;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryQuickslot;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryQuiver;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryStorage;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class GuiStatGroup extends GuiElement {

    private List<GuiStatBase> bars;
    private GuiElement barGroup;

    protected static final int barLength = 59;

    public GuiStatGroup(int x, int y) {
        super(x, y, 200, 52);

        bars = new LinkedList<>();

        barGroup = new GuiElement(0, 0, width, height);
        addChild(barGroup);

        IStatGetter damageGetter = new StatGetterDamage();
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.damage"),
                0, 40, false, damageGetter, LabelGetterBasic.decimalLabel,
                new TooltipGetterDecimal("stats.damage.tooltip", damageGetter)));

        IStatGetter speedGetter = new StatGetterSpeed();
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.speed"),
                0, 4, false, speedGetter, LabelGetterBasic.decimalLabel,
                new TooltipGetterSpeed()));

        IStatGetter durabilityGetter = new StatGetterDurability();
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.durability"),
                0, 2400, false, durabilityGetter, LabelGetterBasic.integerLabel,
                new TooltipGetterInteger("stats.durability.tooltip", durabilityGetter)));

        IStatGetter armorGetter = new StatGetterEffectLevel(ItemEffect.armor, 1d);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.armor"),
                0, 20, false, armorGetter, LabelGetterBasic.integerLabel,
                new TooltipGetterInteger("stats.armor.tooltip", armorGetter)));

        IStatGetter quickslotGetter = new StatGetterEffectLevel(ItemEffect.quickSlot, 1d);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.toolbelt.quickslot"),
                0, InventoryQuickslot.maxSize, true, quickslotGetter, LabelGetterBasic.integerLabel,
                new TooltipGetterInteger("stats.toolbelt.quickslot.tooltip", armorGetter)));

        IStatGetter potionStorageGetter = new StatGetterEffectLevel(ItemEffect.potionSlot, 1d);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.toolbelt.potion_storage"),
                0, InventoryPotions.maxSize, true, potionStorageGetter, LabelGetterBasic.integerLabel,
                new TooltipGetterInteger("stats.toolbelt.potion_storage.tooltip", potionStorageGetter)));

        IStatGetter storageGetter = new StatGetterEffectLevel(ItemEffect.storageSlot, 1d);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.toolbelt.storage"),
                0, InventoryStorage.maxSize, true, storageGetter, LabelGetterBasic.integerLabel,
                new TooltipGetterInteger("stats.toolbelt.storage.tooltip", storageGetter)));

        IStatGetter quiverGetter = new StatGetterEffectLevel(ItemEffect.quiverSlot, 1d);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.toolbelt.quiver"),
                0, InventoryQuiver.maxSize, true, quiverGetter, LabelGetterBasic.integerLabel,
                new TooltipGetterInteger("stats.toolbelt.quiver.tooltip", quiverGetter)));

        IStatGetter boosterGetter = new StatGetterEffectLevel(ItemEffect.booster, 1d);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.toolbelt.booster"),
                0, 3, true, boosterGetter, LabelGetterBasic.integerLabel,
                new TooltipGetterInteger("stats.toolbelt.booster.tooltip", boosterGetter)));

        IStatGetter sweepingGetter = new StatGetterEffectLevel(ItemEffect.sweeping, 12.5);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.sweeping"),
                0, 100, false, sweepingGetter, LabelGetterBasic.percentageLabel,
                new TooltipGetterPercentage("stats.sweeping.tooltip", sweepingGetter)));

        IStatGetter bleedingGetter = new StatGetterEffectLevel(ItemEffect.bleeding, 4);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.bleeding"),
                0, 12, false, bleedingGetter, LabelGetterBasic.integerLabel,
                new TooltipGetterInteger("stats.bleeding.tooltip", bleedingGetter)));

        IStatGetter backstabGetter = new StatGetterEffectLevel(ItemEffect.backstab, 25, 25);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.backstab"),
                0, 200, false, backstabGetter, LabelGetterBasic.percentageLabel,
                new TooltipGetterPercentage("stats.backstab.tooltip", backstabGetter)));

        IStatGetter armorPenetrationGetter = new StatGetterEffectLevel(ItemEffect.armorPenetration, 1);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.armorPenetration"),
                0, 10, false, armorPenetrationGetter, LabelGetterBasic.integerLabel,
                new TooltipGetterInteger("stats.armorPenetration.tooltip", armorPenetrationGetter)));

        IStatGetter unarmoredDamageGetter = new StatGetterEffectLevel(ItemEffect.unarmoredDamage, 1);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.unarmoredDamage"),
                0, 10, false, unarmoredDamageGetter, LabelGetterBasic.integerLabel,
                new TooltipGetterInteger("stats.unarmoredDamage.tooltip", unarmoredDamageGetter)));

        IStatGetter knockbackGetter = new StatGetterEffectLevel(ItemEffect.knockback, 0.5);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.knockback"),
                0, 10, false, knockbackGetter, LabelGetterBasic.decimalLabel,
                new TooltipGetterDecimal("stats.knockback.tooltip", knockbackGetter)));

        IStatGetter lootingGetter = new StatGetterEffectLevel(ItemEffect.looting, 1);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.looting"),
                0, 20, false, lootingGetter, LabelGetterBasic.integerLabel,
                new TooltipGetterInteger("stats.looting.tooltip", lootingGetter)));

        IStatGetter fieryGetter = new StatGetterEffectLevel(ItemEffect.fiery, 4);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.fieryping"),
                0, 32, false, fieryGetter, LabelGetterBasic.integerLabel,
                new TooltipGetterInteger("stats.fiery.tooltip", fieryGetter)));

        IStatGetter smiteGetter = new StatGetterEffectLevel(ItemEffect.smite, 2.5);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.smiteping"),
                0, 25, false, smiteGetter, LabelGetterBasic.decimalLabel,
                new TooltipGetterDecimal("stats.smite.tooltip", smiteGetter)));

        IStatGetter arthropodGetter = new StatGetterEffectLevel(ItemEffect.arthropod, 2.5);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.arthropod"),
                0, 25, false, arthropodGetter, LabelGetterBasic.decimalLabel,
                new TooltipGetterArthropod()));

        IStatGetter unbreakingGetter = new StatGetterUnbreaking();
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.unbreaking"),
                0, 100, false, unbreakingGetter, LabelGetterBasic.percentageLabel,
                new TooltipGetterPercentage("stats.unbreaking.tooltip", unbreakingGetter)));

        IStatGetter mendingGetter = new StatGetterEffectLevel(ItemEffect.mending, 2);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.mending"),
                0, 10, false, mendingGetter, LabelGetterBasic.percentageLabel,
                new TooltipGetterDecimal("stats.mending.tooltip", mendingGetter)));

        IStatGetter silkTouchGetter = new StatGetterEffectLevel(ItemEffect.silkTouch, 1);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.silkTouch"),
                0, 1, false, silkTouchGetter, LabelGetterBasic.percentageLabel,
                new TooltipGetterDecimal("stats.silkTouch.tooltip", silkTouchGetter)));

        IStatGetter fortuneGetter = new StatGetterEffectLevel(ItemEffect.fortune, 1);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.fortune"),
                0, 20, false, fortuneGetter, LabelGetterBasic.integerLabel,
                new TooltipGetterInteger("stats.fortune.tooltip", fortuneGetter)));

        IStatGetter quickStrikeGetter = new StatGetterEffectLevel(ItemEffect.quickStrike, 5, 20);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.quickStrike"),
                0, 100, false, quickStrikeGetter, LabelGetterBasic.percentageLabel,
                new TooltipGetterPercentage("stats.quickStrike.tooltip", quickStrikeGetter)));

        IStatGetter softStrikeGetter = new StatGetterEffectLevel(ItemEffect.softStrike, 1);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.softStrike"),
                0, 1, false, softStrikeGetter, LabelGetterBasic.integerLabel,
                new TooltipGetterInteger("stats.softStrike.tooltip", softStrikeGetter)));

        IStatGetter fierySelfGetter = new StatGetterEffectEfficiency(ItemEffect.fierySelf, 100);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.fierySelf"),
                0, 100, false, fierySelfGetter, LabelGetterBasic.percentageLabel,
                new TooltipGetterPercentage("stats.fierySelf.tooltip", fierySelfGetter)));

        IStatGetter enderReverbGetter = new StatGetterEffectEfficiency(ItemEffect.enderReverb, 100);
        bars.add(new GuiStatBar(0, 0, barLength, I18n.format("stats.enderReverb"),
                0, 100, false, enderReverbGetter, LabelGetterBasic.percentageLabel,
                new TooltipGetterPercentage("stats.enderReverb.tooltip", enderReverbGetter)));

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

        Arrays.stream(Capability.values())
                .map(capability -> new GuiStatBarCapability(0, 0, barLength, capability))
                .forEach(bars::add);

        bars.forEach(bar -> bar.setAttachmentAnchor(GuiAttachment.bottomCenter));
    }

    public void update(ItemStack itemStack, ItemStack previewStack, String slot, String improvement, EntityPlayer player) {
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

    private void realignBar(GuiStatBase bar) {
        int count = barGroup.getNumChildren();

        bar.setY(-17 * ((count % 6) / 2) - 3);

        int xOffset = 3 + (count / 6) * (barLength + 3);
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
