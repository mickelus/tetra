package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.items.ItemModularHandheld;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.util.CastOptional;

public class StatGetterSpeed implements IStatGetter {

    public StatGetterSpeed() { }

    @Override
    public boolean shouldShow(EntityPlayer player, ItemStack currentStack, ItemStack previewStack) {
        return currentStack.getItem() instanceof ItemModularHandheld || previewStack.getItem() instanceof ItemModularHandheld;
    }

    @Override
    public double getValue(EntityPlayer player, ItemStack itemStack) {
        return itemStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).get(SharedMonsterAttributes.ATTACK_SPEED.getName()).stream()
                .map(AttributeModifier::getAmount)
                .reduce(0d, Double::sum) + player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
    }

    @Override
    public double getValue(EntityPlayer player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModule.class))
                .map(module -> module.getSpeedModifier(itemStack) + (module.getSpeedMultiplierModifier(itemStack) - 1) * getValue(player, itemStack))
                .orElse(0d);
    }

    @Override
    public double getValue(EntityPlayer player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> {
                    ImprovementData data = module.getImprovement(itemStack, improvement);
                    return data.damage + (data.damageMultiplier - 1) * module.getDamageModifier(itemStack);
                })
                .orElse(0d);
    }
}
