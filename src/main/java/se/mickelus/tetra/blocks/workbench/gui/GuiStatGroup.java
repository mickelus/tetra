package se.mickelus.tetra.blocks.workbench.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.capabilities.ICapabilityProvider;
import se.mickelus.tetra.gui.GuiAlignment;
import se.mickelus.tetra.gui.GuiElement;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.items.toolbelt.ItemToolbeltModular;


public class GuiStatGroup extends GuiElement {

    private GuiStatBar damageBar;
    private GuiStatBar speedBar;
    private GuiStatBar durabilityBar;
    private GuiStatBar hammerBar;
    private GuiStatBar slotBar;


    public GuiStatGroup(int x, int y) {
        super(x, y, 200, 64);

        damageBar = new GuiStatBar(0, 0, I18n.format("attribute.name.generic.attackDamage"), 0, 40, GuiAlignment.left);
        speedBar = new GuiStatBar(0, 0, I18n.format("attribute.name.generic.attackSpeed"), -4, 4, GuiAlignment.left);
        durabilityBar = new GuiStatBar(0, 0, I18n.format("item.modular.durability"), 0, 1024, GuiAlignment.left);

        hammerBar = new GuiStatBarSegmented(0, 0, I18n.format("item.modular.tier"), 0, 4, GuiAlignment.left);
        slotBar = new GuiStatBarSegmented(0, 0, I18n.format("Slots"), 0, 9, GuiAlignment.left);
    }

    public void setItemStack(ItemStack itemStack, ItemStack previewStack, EntityPlayer player) {
        setVisible(!itemStack.isEmpty());
        if (!itemStack.isEmpty()) {
            clearChildren();
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

                updateHammerBar(getCapability(itemStack, Capability.hammer), getCapability(previewStack, Capability.hammer));
            } else {
                double damage = getAttackDamage(itemStack, player);
                damageBar.setValue(damage, damage);

                double speed = getAttackSpeed(itemStack, player);
                speedBar.setValue(speed, speed);

                durabilityBar.setValue(itemStack.getMaxDamage(), itemStack.getMaxDamage());

                int hammerTier = getCapability(itemStack, Capability.hammer);
                updateHammerBar(hammerTier, hammerTier);
            }

            updateSlotBar(itemStack, previewStack);
        }
    }

    private void showBar(GuiStatBar bar) {
        int offset = getNumChildren();
        bar.setX((1 - offset % 2) * 104);
        bar.setY(30 - 15 * (offset / 2));
        if (offset % 2 == 0) {
            bar.setAlignment(GuiAlignment.left);
        } else {
            bar.setAlignment(GuiAlignment.right);
        }
        addChild(bar);
    }

    private void updateSlotBar(ItemStack itemStack, ItemStack previewStack) {
        if (itemStack.getItem() instanceof ItemToolbeltModular) {
            ItemToolbeltModular item = (ItemToolbeltModular) itemStack.getItem();

            int slots = item.getNumSlots(itemStack);
            if (!previewStack.isEmpty()) {
                slotBar.setValue(slots, item.getNumSlots(previewStack));
            } else {
                slotBar.setValue(slots, slots);
            }

            showBar(slotBar);
        }
    }

    private void updateHammerBar(int tier, int previewTier) {
        if (tier != 0 || previewTier != 0) {
            hammerBar.setValue(tier, previewTier);
            showBar(hammerBar);
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
}
