package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.ICapabilityProvider;
import se.mickelus.tetra.gui.*;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.items.toolbelt.ItemToolbeltModular;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryPotions;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryQuickslot;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryQuiver;
import se.mickelus.tetra.items.toolbelt.inventory.InventoryStorage;


public class GuiStatGroup extends GuiElement {

    private GuiStatBar damageBar;
    private GuiStatBar speedBar;
    private GuiStatBar durabilityBar;

    private GuiStatBar quickslotBar;
    private GuiStatBar potionBar;
    private GuiStatBar storageBar;
    private GuiStatBar quiverBar;
    private GuiStatBar boosterBar;

    private GuiElement barGroup;
    private GuiElement capabilityGroup;


    public GuiStatGroup(int x, int y) {
        super(x, y, 200, 52);

        barGroup = new GuiElement(0, 0, width, height);
        addChild(barGroup);

        damageBar = new GuiStatBar(0, 0, I18n.format("attribute.name.generic.attackDamage"), 0, 40);
        speedBar = new GuiStatBar(0, 0, I18n.format("item.modular.speed"), -4, 4);
        durabilityBar = new GuiStatBar(0, 0, I18n.format("item.modular.durability"), 0, 2024);

        quickslotBar = new GuiStatBarSegmented(0, 0, I18n.format("stats.toolbelt.quickslot"),
                0, InventoryQuickslot.maxSize);
        potionBar = new GuiStatBarSegmented(0, 0, I18n.format("stats.toolbelt.potion_storage"),
                0, InventoryPotions.maxSize);
        storageBar = new GuiStatBarSegmented(0, 0, I18n.format("stats.toolbelt.storage"),
                0, InventoryStorage.maxSize);
        quiverBar = new GuiStatBarSegmented(0, 0, I18n.format("stats.toolbelt.quiver"),
                0, InventoryQuiver.maxSize);
        boosterBar = new GuiStatBarSegmented(0, 0, I18n.format("stats.booster"),
                0, 3);

        capabilityGroup = new GuiElement(width / 2, 0, 0, 0);
        addChild(capabilityGroup);
    }

    public void setItemStack(ItemStack itemStack, ItemStack previewStack, EntityPlayer player) {
        boolean shouldShow = !itemStack.isEmpty() && itemStack.getItem() instanceof ItemModular;
        setVisible(shouldShow);
        if (shouldShow) {
            barGroup.clearChildren();
            if (itemStack.getItem() instanceof ItemModularHandheld) {
                showBar(damageBar);
                showBar(speedBar);
            }

            if (itemStack.getMaxDamage() > 0) {
                showBar(durabilityBar);
            }

            if (!previewStack.isEmpty()) {
                damageBar.setValue(getAttackDamage(itemStack, player), getAttackDamage(previewStack, player));
                speedBar.setValue(getAttackSpeed(itemStack, player), getAttackSpeed(previewStack, player));
                durabilityBar.setValue(itemStack.getMaxDamage(), previewStack.getMaxDamage());
            } else {
                double damage = getAttackDamage(itemStack, player);
                damageBar.setValue(damage, damage);

                double speed = getAttackSpeed(itemStack, player);
                speedBar.setValue(speed, speed);

                durabilityBar.setValue(itemStack.getMaxDamage(), itemStack.getMaxDamage());
            }

            updateToolbeltBars(itemStack, previewStack);

            updateCapabilityIndicators(itemStack, previewStack);
        }
    }

    private void showBar(GuiStatBar bar) {
        int offset = barGroup.getNumChildren();
        bar.setY(-15 * (offset / 2));
        if (offset % 2 == 0) {
            bar.setX(3);
            bar.setAttachmentPoint(GuiAttachment.bottomLeft);
            bar.setAlignment(GuiAlignment.left);
        } else {
            bar.setX(-3);
            bar.setAttachmentPoint(GuiAttachment.bottomRight);
            bar.setAlignment(GuiAlignment.right);
        }
        barGroup.addChild(bar);
    }

    private void updateToolbeltBars(ItemStack itemStack, ItemStack previewStack) {
        if (itemStack.getItem() instanceof ItemToolbeltModular) {
            ItemToolbeltModular item = (ItemToolbeltModular) itemStack.getItem();

            int numQuickslots = item.getNumQuickslots(itemStack);
            if (!previewStack.isEmpty()) {
                int numQuickSlotsPreview = item.getNumQuickslots(previewStack);
                if (numQuickslots > 0 || numQuickSlotsPreview > 0) {
                    quickslotBar.setValue(numQuickslots, numQuickSlotsPreview);
                    showBar(quickslotBar);
                }
            } else if (numQuickslots > 0) {
                quickslotBar.setValue(numQuickslots, numQuickslots);
                showBar(quickslotBar);
            }

            int numPotionSlots = item.getNumPotionSlots(itemStack);
            if (!previewStack.isEmpty()) {
                int numPotionSlotsPreview = item.getNumPotionSlots(previewStack);
                if (numPotionSlots > 0 || numPotionSlotsPreview > 0) {
                    potionBar.setValue(numPotionSlots, numPotionSlotsPreview);
                    showBar(potionBar);
                }
            } else if (numPotionSlots > 0) {
                potionBar.setValue(numPotionSlots, numPotionSlots);
                showBar(potionBar);
            }

            int numStorageSlots = item.getNumStorageSlots(itemStack);
            if (!previewStack.isEmpty()) {
                int numStorageSlotsPreview = item.getNumStorageSlots(previewStack);
                if (numStorageSlots > 0 || numStorageSlotsPreview > 0) {
                    storageBar.setValue(numStorageSlots, numStorageSlotsPreview);
                    showBar(storageBar);
                }
            } else if (numStorageSlots > 0) {
                storageBar.setValue(numStorageSlots, numStorageSlots);
                showBar(storageBar);
            }

            int numQuiverSlots = item.getNumQuiverSlots(itemStack);
            if (!previewStack.isEmpty()) {
                int numQuiverSlotsPreview = item.getNumQuiverSlots(previewStack);
                if (numQuiverSlots > 0 || numQuiverSlotsPreview > 0) {
                    quiverBar.setValue(numQuiverSlots, numQuiverSlotsPreview);
                    showBar(quiverBar);
                }
            } else if (numQuiverSlots > 0) {
                quiverBar.setValue(numQuiverSlots, numQuiverSlots);
                showBar(quiverBar);
            }

            int boostStrength = item.getEffectLevel(itemStack, ItemEffect.booster);
            if (!previewStack.isEmpty()) {
                int boostStrengthPreview = item.getEffectLevel(itemStack, ItemEffect.booster);
                if (boostStrength > 0 || boostStrengthPreview > 0) {
                    boosterBar.setValue(boostStrength, boostStrengthPreview);
                    showBar(boosterBar);
                }
            } else if (boostStrength > 0) {
                boosterBar.setValue(boostStrength, boostStrength);
                showBar(boosterBar);
            }
        }
    }

    private double getAttackDamage(ItemStack itemStack, EntityPlayer player) {
        return itemStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()).stream()
                .map(AttributeModifier::getAmount)
                .reduce(0d, Double::sum) + player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
    }

    private double getAttackSpeed(ItemStack itemStack, EntityPlayer player) {
        return itemStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_SPEED.getName()).stream()
                .map(AttributeModifier::getAmount)
                .reduce(0d, Double::sum) + player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
    }

    private int getCapability(ItemStack itemStack, Capability capability) {
        if (itemStack.getItem() instanceof ICapabilityProvider) {
            ICapabilityProvider item = (ICapabilityProvider) itemStack.getItem();
            return item.getCapabilityLevel(itemStack, capability);
        }
        return 0;
    }

    private void updateCapabilityIndicators(ItemStack itemStack, ItemStack previewStack) {
        Capability[] capabilities = Capability.values();
        capabilityGroup.clearChildren();

        if (previewStack.isEmpty()) {
            previewStack = itemStack;
        }

        for (int i = 0; i < capabilities.length; i++) {
            int previewLevel = getCapability(previewStack, capabilities[i]);
            int currentLevel = getCapability(itemStack, capabilities[i]);
            if (previewLevel > 0 || currentLevel > 0) {
                GuiCapability guiCapability = new GuiCapability(capabilityGroup.getNumChildren() * 16, 0, capabilities[i]);
                if (previewLevel > currentLevel) {
                    guiCapability.update(previewLevel, GuiColors.add);
                } else if (previewLevel < currentLevel) {
                    guiCapability.update(previewLevel, GuiColors.remove);
                } else {
                    guiCapability.update(previewLevel, GuiColors.normal);
                }
                capabilityGroup.addChild(guiCapability);
            }
        }

        capabilityGroup.setX(width / 2 - 8 * (capabilityGroup.getNumChildren()));

    }
}
