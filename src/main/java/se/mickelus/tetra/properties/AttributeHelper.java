package se.mickelus.tetra.properties;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.util.math.MathHelper;
import se.mickelus.tetra.items.modular.ModularItem;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AttributeHelper {

    public static final Multimap<Attribute, AttributeModifier> emptyMap = ImmutableMultimap.of();

    private static final Map<String, UUID> attributeIdMap = new HashMap<>();
    static {
        attributeIdMap.put(getAttributeKey(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADDITION), ModularItem.attackDamageModifier);
        attributeIdMap.put(getAttributeKey(Attributes.ATTACK_SPEED, AttributeModifier.Operation.ADDITION), ModularItem.attackSpeedModifier);
    }

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
    public static Collection<AttributeModifier> collapse(Collection<AttributeModifier> modifiers) {
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
        return getMergedAmount(modifiers, 0);
    }

    public static double getMergedAmount(Collection<AttributeModifier> modifiers, double base) {
        return (getAdditionAmount(modifiers) + base) * getMultiplyAmount(modifiers);
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
                .map(amount -> amount * Math.abs(base))
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

    public static Multimap<Attribute, AttributeModifier> collapseRound(Multimap<Attribute, AttributeModifier> modifiers) {
        return Optional.ofNullable(modifiers)
                .map(Multimap::asMap)
                .map(Map::entrySet)
                .map(Collection::stream)
                .map(entries -> entries.collect(Multimaps.flatteningToMultimap(
                        Map.Entry::getKey,
                        entry -> AttributeHelper.collapse(entry.getValue()).stream(),
                        ArrayListMultimap::create)))
                .map(AttributeHelper::round)
                .orElse(null);
    }

    public static Multimap<Attribute, AttributeModifier> round(Multimap<Attribute, AttributeModifier> modifiers) {
        return Optional.ofNullable(modifiers)
                .map(Multimap::entries)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .collect(Multimaps.toMultimap(Map.Entry::getKey, e -> round(e.getKey(), e.getValue()), ArrayListMultimap::create));
    }

    private static AttributeModifier round(Attribute attribute, AttributeModifier mod) {
        double multiplier = (Attributes.ATTACK_DAMAGE.equals(attribute)
                || Attributes.ARMOR.equals(attribute)
                || Attributes.ARMOR_TOUGHNESS.equals(attribute)
                || TetraAttributes.drawStrength.get().equals(attribute)
                || TetraAttributes.abilityDamage.get().equals(attribute))
                && mod.getOperation() == AttributeModifier.Operation.ADDITION
                ? 2 : 20;
        return new AttributeModifier(mod.getID(), mod.getName(), Math.round(mod.getAmount() * multiplier) / multiplier, mod.getOperation());
//        return mod;
    }

    public static String getAttributeKey(Attribute attribute, AttributeModifier.Operation operation) {
        return attribute.getAttributeName() + operation.ordinal();
    }

    private static UUID getAttributeId(Attribute attribute, AttributeModifier.Operation operation) {
        return attributeIdMap.computeIfAbsent(getAttributeKey(attribute, operation), k -> MathHelper.getRandomUUID(ThreadLocalRandom.current()));
    }

    public static AttributeModifier fixIdentifiers(Attribute attribute, AttributeModifier modifier) {
        return new AttributeModifier(getAttributeId(attribute, modifier.getOperation()), modifier.getName(), modifier.getAmount(), modifier.getOperation());
    }

    public static Multimap<Attribute, AttributeModifier> fixIdentifiers(Multimap<Attribute, AttributeModifier> modifiers) {
        return Optional.ofNullable(modifiers)
                .map(Multimap::entries)
                .map(Collection::stream)
                .map(entries -> entries.collect(Multimaps.toMultimap(
                        Map.Entry::getKey,
                        entry -> fixIdentifiers(entry.getKey(), entry.getValue()),
                        ArrayListMultimap::create)))
                .orElse(null);
    }
}
