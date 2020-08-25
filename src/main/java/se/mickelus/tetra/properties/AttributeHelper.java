package se.mickelus.tetra.properties;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AttributeHelper {

    public static final Multimap<Attribute, AttributeModifier> emptyMap = ImmutableMultimap.of();

    /**
     * Merge two multimaps, values from b will be used when both map contain values for the same key
     * @param a
     * @param b
     * @return
     */
    public static Multimap<Attribute, AttributeModifier> overwrite(Multimap<Attribute, AttributeModifier> a, Multimap<Attribute, AttributeModifier> b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }

        ArrayListMultimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();

        result.putAll(a);
        b.asMap().forEach(result::replaceValues);

        return result;
    }

    public static Multimap<Attribute, AttributeModifier> merge(Collection<Multimap<Attribute, AttributeModifier>> modifiers) {
        return modifiers.stream().reduce(null, AttributeHelper::merge);
    }

    public static Multimap<Attribute, AttributeModifier> merge(Multimap<Attribute, AttributeModifier> a, Multimap<Attribute, AttributeModifier> b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }

        ArrayListMultimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();

        result.putAll(a);
        b.forEach(result::put);

        return result;
    }

    public static Multimap<Attribute, AttributeModifier> retainMax(Multimap<Attribute, AttributeModifier> modifiers, Attribute... attributes) {
        return retainMax(modifiers, Arrays.asList(attributes));
    }

    public static Multimap<Attribute, AttributeModifier> retainMax(Multimap<Attribute, AttributeModifier> modifiers, Collection<Attribute> attributes) {
        if (modifiers == null) {
            return null;
        }

        // old solution, not fully commited, keep around in case new solution borks
//        Map<Attribute, AttributeModifier> maxValues = modifiers.entries().stream()
//                .filter(entry -> attributes.contains(entry.getKey()) && entry.getValue().getOperation() == AttributeModifier.Operation.ADDITION)
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a.getAmount() >= b.getAmount() ? a : b));
//
//        return modifiers.entries().stream()
//                .filter(entry -> !maxValues.containsKey(entry.getKey())
//                        || entry.getValue().getOperation() != AttributeModifier.Operation.ADDITION
//                        || entry.getValue() == maxValues.get(entry.getKey()))
//                .collect(Multimaps.toMultimap(Map.Entry::getKey, Map.Entry::getValue, ArrayListMultimap::create));

        return modifiers.asMap().entrySet().stream()
                .collect(Multimaps.flatteningToMultimap(
                        Map.Entry::getKey,
                        entry -> attributes.contains(entry.getKey()) ? retainMax(entry.getValue()).stream() : entry.getValue().stream(),
                        ArrayListMultimap::create));
    }

    public static Collection<AttributeModifier> retainMax(Collection<AttributeModifier> modifiers) {
        return modifiers.stream()
                .collect(Collectors.groupingBy(AttributeModifier::getOperation, Collectors.maxBy(Comparator.comparing(AttributeModifier::getAmount))))
                .values()
                .stream()
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Collapse the modifiers collection into two entries per attribute: ADDITION & MULTIPLY_TOTAL. ADDITION is aggregated from ADDITION and
     * MULTIPLY_BASE so that MULTIPLY_BASE can be used by improvements to increase attributes based on the module value.
     * @param modifiers
     * @return
     */
    public static Collection<AttributeModifier> collapseModifiers(Collection<AttributeModifier> modifiers) {
        return Stream.of(
                Optional.of(getAdditionAmount(modifiers))
                        .filter(amount -> amount != 0)
                        .map(amount -> new AttributeModifier("tetra.stats.addition", amount, AttributeModifier.Operation.ADDITION)),
                Optional.of(getMultiplyAmount(modifiers))
                        .map(amount -> amount - 1) // vanilla expects the multiplier to be 0 based
                        .filter(amount -> amount != 0)
                        .map(amount -> new AttributeModifier("tetra.stats.multiply", amount, AttributeModifier.Operation.MULTIPLY_TOTAL)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Computes an aggregated value from all modifiers
     * Based on {@link net.minecraft.entity.ai.attributes.ModifiableAttributeInstance#ModifiableAttributeInstance}
     * @param modifiers
     * @return
     */
    public static double getMergedAmount(Collection<AttributeModifier> modifiers) {
        return getAdditionAmount(modifiers) * getMultiplyAmount(modifiers);
    }

    public static double getAdditionAmount(Collection<AttributeModifier> modifiers) {
        double base = modifiers.stream()
                .filter(modifier -> modifier.getOperation().equals(AttributeModifier.Operation.ADDITION))
                .mapToDouble(AttributeModifier::getAmount)
                .sum();

        return base
                + modifiers.stream()
                .filter(modifier -> modifier.getOperation().equals(AttributeModifier.Operation.MULTIPLY_BASE))
                .mapToDouble(AttributeModifier::getAmount)
                .map(amount -> amount * base)
                .sum();
    }

    public static double getMultiplyAmount(Collection<AttributeModifier> modifiers) {
        return modifiers.stream()
                .filter(modifier -> modifier.getOperation().equals(AttributeModifier.Operation.MULTIPLY_TOTAL))
                .mapToDouble(AttributeModifier::getAmount)
                .map(amount -> amount + 1)
                .reduce(1, (a, b) -> a * b);
    }

    public static Multimap<Attribute, AttributeModifier> multiplyModifiers(Multimap<Attribute, AttributeModifier> modifiers, double multiplier) {
        return Optional.ofNullable(modifiers)
                .map(Multimap::entries)
                .map(Collection::stream)
                .map(entries -> entries.collect(Multimaps.toMultimap(
                        Map.Entry::getKey,
                        entry -> multiplyModifier(entry.getValue(), multiplier),
                        ArrayListMultimap::create)))
                .orElse(null);
    }

    public static AttributeModifier multiplyModifier(AttributeModifier modifier, double multiplier) {
        return new AttributeModifier(modifier.getID(), modifier.getName(), modifier.getAmount() * multiplier, modifier.getOperation());
    }
}
