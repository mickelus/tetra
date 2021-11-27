package se.mickelus.tetra.gui.stats.getter;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.properties.AttributeHelper;
import se.mickelus.tetra.util.CastOptional;

import java.util.Optional;

public class StatGetterAttribute implements IStatGetter {
    private final Attribute attribute;

    private boolean ignoreBase = false;
    private boolean ignoreBonuses = false;

    private double offset = 0;

    public StatGetterAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public StatGetterAttribute(Attribute attribute, boolean ignoreBase) {
        this(attribute);

        this.ignoreBase = ignoreBase;
    }

    public StatGetterAttribute withOffset(double offset) {
        this.offset = offset;
        return this;
    }

    public StatGetterAttribute(Attribute attribute, boolean ignoreBase, boolean ignoreBonuses) {
        this(attribute);

        this.ignoreBase = ignoreBase;
        this.ignoreBonuses = ignoreBonuses;
    }

    @Override
    public boolean shouldShow(PlayerEntity player, ItemStack currentStack, ItemStack previewStack) {
        double baseValue = ignoreBase ? 0 : Optional.ofNullable(player.getAttribute(attribute))
                .map(ModifiableAttributeInstance::getBaseValue)
                .orElse(0d) + offset;
        return getValue(player, currentStack) != baseValue || getValue(player, previewStack) != baseValue;
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack) {
        double baseValue = ignoreBase ? 0 : Optional.ofNullable(player.getAttribute(attribute))
                .map(ModifiableAttributeInstance::getBaseValue)
                .orElse(0d);
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> ignoreBonuses ? item.getModuleAttributes(itemStack) : item.getAttributeModifiers(itemStack))
                .map(map -> map.get(attribute))
                .map(modifiers -> (AttributeHelper.getAdditionAmount(modifiers) + baseValue) * AttributeHelper.getMultiplyAmount(modifiers))
                .orElse(baseValue) + offset;
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getAttributeModifiers(itemStack))
                .map(map -> map.get(attribute))
                .map(modifiers -> AttributeHelper.getAdditionAmount(modifiers) + (AttributeHelper.getMultiplyAmount(modifiers) - 1) * getValue(player, itemStack))
                .orElse(0d);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), IModularItem.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> module.getImprovement(itemStack, improvement))
                .map(improvementData -> improvementData.attributes)
                .map(map -> map.get(attribute))
                .map(modifiers -> AttributeHelper.getAdditionAmount(modifiers) + (AttributeHelper.getMultiplyAmount(modifiers) - 1) * getValue(player, itemStack))
                .orElse(0d);
    }
}
