package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.util.CastOptional;

import java.util.Optional;

public class StatGetterAttributeMultiply implements IStatGetter {
    private final Attribute attribute;

    public StatGetterAttributeMultiply(Attribute attribute) {
        this.attribute = attribute;
    }


    @Override
    public boolean shouldShow(PlayerEntity player, ItemStack currentStack, ItemStack previewStack) {
        return getValue(player, currentStack) != 0 || getValue(player, previewStack) != 0;
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getAttributeModifiers(itemStack))
                .map(map -> map.get(attribute))
                .map(AttributeHelper::getMultiplyAmount)
                .orElseGet(attribute::getDefaultValue);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getAttributeModifiers(itemStack))
                .map(map -> map.get(attribute))
                .map(AttributeHelper::getMultiplyAmount)
                .orElse(0d);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getImprovement(itemStack, improvement))
                .map(improvementData -> improvementData.attributes)
                .map(map -> map.get(attribute))
                .map(AttributeHelper::getMultiplyAmount)
                .orElse(0d);
    }
}
