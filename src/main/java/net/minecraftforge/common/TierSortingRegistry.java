package net.minecraftforge.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TierSortingRegistry {
    private static final List<Tier> sortedTiers = new ArrayList<>();
    private static final Map<Tier, ResourceLocation> namesByTier = new IdentityHashMap<>();
    private static final Map<ResourceLocation, Tier> tiersByName = new LinkedHashMap<>();

    static {
        registerVanilla(Tiers.WOOD, "wood");
        registerVanilla(Tiers.STONE, "stone");
        registerVanilla(Tiers.IRON, "iron");
        registerVanilla(Tiers.DIAMOND, "diamond");
        registerVanilla(Tiers.NETHERITE, "netherite");
        registerVanilla(Tiers.GOLD, "gold");
    }

    private TierSortingRegistry() {}

    private static void registerVanilla(Tier tier, String path) {
        ResourceLocation name = ResourceLocation.withDefaultNamespace(path);
        sortedTiers.add(tier);
        namesByTier.put(tier, name);
        tiersByName.put(name, tier);
    }

    public static Tier registerTier(Tier tier, ResourceLocation name, List<Tier> after, List<Tier> before) {
        if (!namesByTier.containsKey(tier)) {
            int index = after.stream()
                    .mapToInt(sortedTiers::indexOf)
                    .max()
                    .orElse(sortedTiers.size() - 1);
            sortedTiers.add(Math.min(index + 1, sortedTiers.size()), tier);
            namesByTier.put(tier, name);
            tiersByName.put(name, tier);
        }

        return tier;
    }

    public static Tier byName(ResourceLocation name) {
        return tiersByName.get(name);
    }

    public static List<Tier> getSortedTiers() {
        return List.copyOf(sortedTiers);
    }

    public static ResourceLocation getName(Tier tier) {
        return namesByTier.get(tier);
    }

    public static boolean isCorrectTierForDrops(Tier tier, BlockState state) {
        return !state.is(tier.getIncorrectBlocksForDrops());
    }

    private static int getTierLevel(Tier tier) {
        if (tier instanceof ForgeTier forgeTier) {
            return forgeTier.getLevel();
        }
        if (tier == Tiers.WOOD || tier == Tiers.GOLD) {
            return 0;
        }
        if (tier == Tiers.STONE) {
            return 1;
        }
        if (tier == Tiers.IRON) {
            return 2;
        }
        if (tier == Tiers.DIAMOND) {
            return 3;
        }
        if (tier == Tiers.NETHERITE) {
            return 4;
        }
        return sortedTiers.indexOf(tier);
    }
}
