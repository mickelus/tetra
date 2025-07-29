package se.mickelus.tetra.effect.data;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemEffectContext {
    private LivingEntity usingEntity;
    private ItemStack usedItemStack;
    private Level level;
    private Map<String, Float> numbers;
    private Map<String, Vec3> vectors;
    private Map<String, Entity> entities;

    public ItemEffectContext(LivingEntity usingEntity, ItemStack usedItemStack, Level level,
            Map<String, Float> numbers, Map<String, Vec3> vectors, Map<String, Entity> entities) {
        this.usingEntity = usingEntity;
        this.usedItemStack = usedItemStack;
        this.level = level;

        this.numbers = numbers;
        this.vectors = vectors;
        this.entities = entities;
    }

    public ItemEffectContext(LivingEntity usingEntity, ItemStack usedItemStack, Level level) {
        this(usingEntity, usedItemStack, level, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }


    public ItemEffectContext copy() {
        return new ItemEffectContext(usingEntity, usedItemStack, level, numbers, vectors, entities);
    }

    public ItemEffectContext withNumbers(Map<String, Float> numbers) {
        ItemEffectContext copy = copy();
        copy.numbers = numbers;
        return copy;
    }

    public ItemEffectContext withMergedNumbers(Map<String, Float> numbers) {
        ItemEffectContext copy = copy();
        copy.numbers = mergeNumbers(copy.numbers, numbers);
        return copy;
    }

    @SafeVarargs
    public static Map<String, Float> mergeNumbers(Map<String, Float>... numbers) {
        return Stream.of(numbers)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));
    }

    public ItemEffectContext withVectors(Map<String, Vec3> vectors) {
        ItemEffectContext copy = copy();
        copy.vectors = vectors;
        return copy;
    }

    public ItemEffectContext withMergedVectors(Map<String, Vec3> vectors) {
        ItemEffectContext copy = copy();
        copy.vectors = mergeVectors(copy.vectors, vectors);
        return copy;
    }

    @SafeVarargs
    public static Map<String, Vec3> mergeVectors(Map<String, Vec3>... vectors) {
        return Stream.of(vectors)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));
    }

    public ItemEffectContext withEntities(Map<String, Entity> entities) {
        ItemEffectContext copy = copy();
        copy.entities = entities;
        return copy;
    }

    public ItemEffectContext withMergedEntities(Map<String, Entity> entities) {
        ItemEffectContext copy = copy();
        copy.entities = mergeEntities(copy.entities, entities);
        return copy;
    }

    @SafeVarargs
    public static Map<String, Entity> mergeEntities(Map<String, Entity>... entities) {
        return Stream.of(entities)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));
    }

    public ItemStack getUsedItemStack() {
        return usedItemStack;
    }

    public Level getLevel() {
        return level;
    }

    public Map<String, Float> getNumbers() {
        return numbers;
    }

    public Map<String, Vec3> getVectors() {
        return vectors;
    }

    public Map<String, Entity> getEntities() {
        return entities;
    }
}
