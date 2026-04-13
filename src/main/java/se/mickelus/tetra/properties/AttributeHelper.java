package se.mickelus.tetra.properties;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.modular.ModularItem;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class AttributeHelper {

    public static final Multimap<Attribute, AttributeModifier> emptyMap = ImmutableMultimap.of();

    private static final Map<String, ResourceLocation> attributeIdMap = new HashMap<>();

    static {
        attributeIdMap.put(getAttributeKey(Attributes.ATTACK_DAMAGE.value(), AttributeModifier.Operation.ADD_VALUE),
                ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "attack_damage"));
        attributeIdMap.put(getAttributeKey(Attributes.ATTACK_SPEED.value(), AttributeModifier.Operation.ADD_VALUE),
                ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "attack_speed"));
    }

    public static Holder<Attribute> getHolder(Attribute attribute) {
        ResourceLocation key = Objects.requireNonNull(BuiltInRegistries.ATTRIBUTE.getKey(attribute), "Unregistered attribute: " + attribute);
        Holder.Reference<Attribute> holder = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceKey.create(Registries.ATTRIBUTE, key)).orElse(null);
        return holder != null ? holder : BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute);
    }

    /**
     * Merge two multimaps, values from b will be used when both map contain values for the same key
     *
     * @param a
     * @param b
     * @return
     */
    public static Multimap<Attribute, AttributeModifier> overwrite(Multimap<Attribute, AttributeModifier> a,
            Multimap<Attribute, AttributeModifier> b) {
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

    public static Multimap<Attribute, AttributeModifier> retainMax(Multimap<Attribute, AttributeModifier> modifiers,
            Collection<Attribute> attributes) {
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
                .collect(Collectors.groupingBy(AttributeModifier::operation, Collectors.maxBy(Comparator.comparing(AttributeModifier::amount))))
                .values()
                .stream()
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Collapse the modifiers collection into two entries per attribute: ADDITION & MULTIPLY_TOTAL. ADDITION is aggregated from ADDITION and
     * MULTIPLY_BASE so that MULTIPLY_BASE can be used by improvements to increase attributes based on the module value.
     *
     * @param modifiers
     * @return
     */
    public static Collection<AttributeModifier> collapse(Collection<AttributeModifier> modifiers) {
        return Stream.of(
                        Optional.of(getAdditionAmount(modifiers))
                                .filter(amount -> amount != 0)
                                .map(amount -> new AttributeModifier(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "stats/addition"), amount, AttributeModifier.Operation.ADD_VALUE)),
                        Optional.of(getMultiplyAmount(modifiers))
                                .map(amount -> amount - 1) // vanilla expects the multiplier to be 0 based
                                .filter(amount -> amount != 0)
                                .map(amount -> new AttributeModifier(ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID, "stats/multiply"), amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Computes an aggregated value from all modifiers
     * Based on {@link net.minecraft.entity.ai.attributes.ModifiableAttributeInstance#ModifiableAttributeInstance}
     *
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
                .filter(modifier -> modifier.operation().equals(AttributeModifier.Operation.ADD_VALUE))
                .mapToDouble(AttributeModifier::amount)
                .sum();

        return base
                + modifiers.stream()
                .filter(modifier -> modifier.operation().equals(AttributeModifier.Operation.ADD_MULTIPLIED_BASE))
                .mapToDouble(AttributeModifier::amount)
                .map(amount -> amount * Math.abs(base))
                .sum();
    }

    public static double getMultiplyAmount(Collection<AttributeModifier> modifiers) {
        return modifiers.stream()
                .filter(modifier -> modifier.operation().equals(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))
                .mapToDouble(AttributeModifier::amount)
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
        return new AttributeModifier(modifier.id(), modifier.amount() * multiplier, modifier.operation());
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

    private static float getRounding(Attribute attribute, AttributeModifier mod) {
        if (mod.operation() != AttributeModifier.Operation.ADD_VALUE) {
            return 0.01f;
        } else if (Attributes.ATTACK_DAMAGE.value().equals(attribute)
                || Attributes.ARMOR.value().equals(attribute)
                || Attributes.ARMOR_TOUGHNESS.value().equals(attribute)
                || TetraAttributes.drawStrength.get().equals(attribute)
                || TetraAttributes.abilityDamage.get().equals(attribute)) {
            return 0.5f;
        }
        return 0.05f;
    }

    private static AttributeModifier round(Attribute attribute, AttributeModifier mod) {
        double rounding = getRounding(attribute, mod);
        return new AttributeModifier(mod.id(), Math.round(mod.amount() / rounding) * rounding, mod.operation());
    }

    public static String getAttributeKey(Attribute attribute, AttributeModifier.Operation operation) {
        return attribute.getDescriptionId() + operation.ordinal();
    }

    private static ResourceLocation getAttributeId(Attribute attribute, AttributeModifier.Operation operation) {
        return attributeIdMap.computeIfAbsent(getAttributeKey(attribute, operation),
                k -> ResourceLocation.fromNamespaceAndPath(TetraMod.MOD_ID,
                        "attribute/" + attribute.getDescriptionId().replace(':', '_').replace('.', '_') + "/" + operation.getSerializedName()));
    }

    public static AttributeModifier fixIdentifiers(Attribute attribute, AttributeModifier modifier) {
        return new AttributeModifier(getAttributeId(attribute, modifier.operation()), modifier.amount(), modifier.operation());
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


    public static double calculateValue(Attribute attribute, Collection<AttributeModifier>... modifiers) {
        double sum = Arrays.stream(modifiers)
                .flatMap(Collection::stream)
                .filter(modifier -> modifier.operation() == AttributeModifier.Operation.ADD_VALUE)
                .mapToDouble(AttributeModifier::amount)
                .sum() + attribute.getDefaultValue();

        double additiveMultiplier = Arrays.stream(modifiers)
                .flatMap(Collection::stream)
                .filter(modifier -> modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                .mapToDouble(AttributeModifier::amount)
                .sum() + 1;

        double multiplicativeMultiplier = Arrays.stream(modifiers)
                .flatMap(Collection::stream)
                .filter(modifier -> modifier.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .mapToDouble(modifier -> modifier.amount() + 1)
                .reduce(1, (a, b) -> a * b);

        return attribute.sanitizeValue(sum * additiveMultiplier * multiplicativeMultiplier);
    }
}
