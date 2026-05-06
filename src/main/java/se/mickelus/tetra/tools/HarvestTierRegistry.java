package se.mickelus.tetra.tools;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class HarvestTierRegistry {
    private static final Logger logger = LogManager.getLogger();
    private static final List<Tier> orderedTiers = new ArrayList<>();
    private static final Map<Tier, TierEntry> entriesByTier = new IdentityHashMap<>();
    private static final Map<ResourceLocation, Tier> tiersByName = new LinkedHashMap<>();
    private static final Set<ResourceLocation> unknownTierWarnings = ConcurrentHashMap.newKeySet();
    private static int nextOrder;

    static {
        registerVanilla(Tiers.WOOD, "wood", List.of(), List.of());
        registerVanilla(Tiers.GOLD, "gold", List.of(Tiers.WOOD), List.of());
        registerVanilla(Tiers.STONE, "stone", List.of(Tiers.GOLD), List.of());
        registerVanilla(Tiers.IRON, "iron", List.of(Tiers.STONE), List.of());
        registerVanilla(Tiers.DIAMOND, "diamond", List.of(Tiers.IRON), List.of());
        registerVanilla(Tiers.NETHERITE, "netherite", List.of(Tiers.DIAMOND), List.of());
    }

    private HarvestTierRegistry() {}

    private static void registerVanilla(Tier tier, String path, List<Tier> after, List<Tier> before) {
        registerInternal(tier, ResourceLocation.withDefaultNamespace(path), after, before);
    }

    public static Tier register(Tier tier, ResourceLocation name, List<Tier> after, List<Tier> before) {
        Objects.requireNonNull(tier, "tier");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(after, "after");
        Objects.requireNonNull(before, "before");

        if (entriesByTier.containsKey(tier)) {
            ResourceLocation existingName = nameOf(tier);
            if (!name.equals(existingName)) {
                throw new IllegalStateException("Tier " + tier + " is already registered as " + existingName + ", not " + name);
            }
            return tier;
        }

        registerInternal(tier, name, after, before);
        return tier;
    }

    private static void registerInternal(Tier tier, ResourceLocation name, List<Tier> after, List<Tier> before) {
        Tier namedTier = tiersByName.get(name);
        if (namedTier != null && namedTier != tier) {
            throw new IllegalStateException("Tier name already registered: " + name);
        }

        validateDependencies(name, after, "after");
        validateDependencies(name, before, "before");

        entriesByTier.put(tier, new TierEntry(name, List.copyOf(after), List.copyOf(before), nextOrder++));
        tiersByName.put(name, tier);
        rebuildOrderedTiers();
    }

    private static void validateDependencies(ResourceLocation name, List<Tier> dependencies, String direction) {
        for (Tier dependency : dependencies) {
            if (!entriesByTier.containsKey(dependency)) {
                throw new IllegalStateException("Unknown " + direction + " dependency for tier " + name + ": " + dependency);
            }
        }
    }

    private static void rebuildOrderedTiers() {
        Map<Tier, Set<Tier>> edges = new IdentityHashMap<>();
        Map<Tier, Integer> indegree = new IdentityHashMap<>();

        for (Tier tier : entriesByTier.keySet()) {
            edges.put(tier, new LinkedHashSet<>());
            indegree.put(tier, 0);
        }

        for (Map.Entry<Tier, TierEntry> entry : entriesByTier.entrySet()) {
            Tier tier = entry.getKey();
            TierEntry data = entry.getValue();

            for (Tier dependency : data.after()) {
                addEdge(edges, indegree, dependency, tier);
            }
            for (Tier dependency : data.before()) {
                addEdge(edges, indegree, tier, dependency);
            }
        }

        PriorityQueue<Tier> ready = new PriorityQueue<>(Comparator.comparingInt(tier -> entriesByTier.get(tier).order()));
        for (Map.Entry<Tier, Integer> entry : indegree.entrySet()) {
            if (entry.getValue() == 0) {
                ready.add(entry.getKey());
            }
        }

        List<Tier> resolved = new ArrayList<>(entriesByTier.size());
        while (!ready.isEmpty()) {
            Tier tier = ready.remove();
            resolved.add(tier);

            for (Tier dependent : edges.get(tier)) {
                int remaining = indegree.computeIfPresent(dependent, (ignored, value) -> value - 1);
                if (remaining == 0) {
                    ready.add(dependent);
                }
            }
        }

        if (resolved.size() != entriesByTier.size()) {
            String cycle = entriesByTier.entrySet().stream()
                    .filter(entry -> indegree.get(entry.getKey()) > 0)
                    .sorted(Comparator.comparingInt(entry -> entry.getValue().order()))
                    .map(entry -> entry.getValue().name().toString())
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException("Cyclic tier dependencies detected: " + cycle);
        }

        orderedTiers.clear();
        orderedTiers.addAll(resolved);
    }

    private static void addEdge(Map<Tier, Set<Tier>> edges, Map<Tier, Integer> indegree, Tier source, Tier target) {
        if (edges.get(source).add(target)) {
            indegree.computeIfPresent(target, (ignored, value) -> value + 1);
        }
    }

    @Nullable
    public static Tier byName(@Nullable ResourceLocation name) {
        if (name == null) {
            return null;
        }

        Tier tier = tiersByName.get(name);
        if (tier == null && unknownTierWarnings.add(name)) {
            logger.warn("Unknown harvest tier '{}', falling back to level 0", name);
        }
        return tier;
    }

    public static List<Tier> ordered() {
        return List.copyOf(orderedTiers);
    }

    @Nullable
    public static ResourceLocation nameOf(Tier tier) {
        TierEntry entry = entriesByTier.get(tier);
        return entry != null ? entry.name() : null;
    }

    public static boolean isCorrectTierForDrops(Tier tier, BlockState state) {
        return !state.is(tier.getIncorrectBlocksForDrops());
    }

    private record TierEntry(ResourceLocation name, List<Tier> after, List<Tier> before, int order) {}
}
