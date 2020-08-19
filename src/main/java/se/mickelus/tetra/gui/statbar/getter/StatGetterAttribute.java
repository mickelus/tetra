package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.util.CastOptional;

import java.util.Optional;

public class StatGetterAttribute implements IStatGetter {
    private final Attribute attribute;

    public StatGetterAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public boolean shouldShow(PlayerEntity player, ItemStack currentStack, ItemStack previewStack) {
        double baseValue = attribute.getDefaultValue() + Optional.ofNullable(player.getAttribute(attribute))
                .map(ModifiableAttributeInstance::getBaseValue)
                .orElse(0d);
        return getValue(player, currentStack) != baseValue || getValue(player, previewStack) != baseValue;
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack) {
        double baseValue = Optional.ofNullable(player.getAttribute(attribute))
                .map(ModifiableAttributeInstance::getBaseValue)
                .orElse(0d);
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .map(item -> item.getAttributeModifiers(itemStack))
                .map(map -> map.get(attribute))
                .map(modifiers -> (AttributeHelper.getAdditionAmount(modifiers) + baseValue) * AttributeHelper.getMultiplyAmount(modifiers))
                .orElseGet(attribute::getDefaultValue);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getAttributeModifiers(itemStack))
                .map(map -> map.get(attribute))
                .map(modifiers -> AttributeHelper.getAdditionAmount(modifiers) + (AttributeHelper.getMultiplyAmount(modifiers) - 1) * getValue(player, itemStack))
                .orElse(0d);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getImprovement(itemStack, improvement))
                .map(improvementData -> improvementData.attributes)
                .map(map -> map.get(attribute))
                .map(modifiers -> AttributeHelper.getAdditionAmount(modifiers) + (AttributeHelper.getMultiplyAmount(modifiers) - 1) * getValue(player, itemStack))
                .orElse(0d);
    }
}
