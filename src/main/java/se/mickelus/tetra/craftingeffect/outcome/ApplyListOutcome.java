package se.mickelus.tetra.craftingeffect.outcome;

import com.google.common.collect.Streams;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import net.minecraftforge.common.util.LazyOptional;
import se.mickelus.tetra.craftingeffect.CraftingEffect;
import se.mickelus.tetra.craftingeffect.CraftingEffectRegistry;
import se.mickelus.tetra.craftingeffect.condition.CraftingEffectCondition;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.util.StreamHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class ApplyListOutcome implements CraftingEffectOutcome {

    ResourceLocation[] references = new ResourceLocation[0];
    EffectPair[] effects = new EffectPair[0];
    boolean random = false;
    int count = Integer.MAX_VALUE;

    public ApplyListOutcome() {
    }

    public ApplyListOutcome(EffectPair[] effects) {
        this.effects = effects;
    }

    LazyOptional<EffectPair[]> resolvedReferences = LazyOptional.of(() -> resolveReferences(references));

    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] preMaterials,
            Map<ItemAbility, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos pos, BlockState blockState, boolean consumeResources,
            ItemStack[] postMaterials, float severity) {
        Collector<EffectPair, ?, List<EffectPair>> collector = random
                ? StreamHelper.toShuffledList()
                : Collectors.toUnmodifiableList();

        List<EffectPair> applicableOutcomes = Streams.concat(Arrays.stream(effects), resolvedReferences.lazyMap(Arrays::stream).orElseGet(Stream::empty))
                .filter(outcome -> outcome.requirement().test(unlockedEffects, upgradedStack, slot, isReplacing, player, preMaterials, tools, schematic,
                        world, pos, blockState))
                .collect(collector);

        for (int i = 0; i < applicableOutcomes.size() & i < count; i++) {
            applicableOutcomes.get(i).outcome().apply(unlockedEffects, upgradedStack, slot, isReplacing, player, preMaterials, tools, world, schematic, pos, blockState, consumeResources, postMaterials, severity);
        }
        return !applicableOutcomes.isEmpty();
    }

    private static EffectPair[] resolveReferences(ResourceLocation[] references) {
        return Arrays.stream(CraftingEffectRegistry.getEffects(references))
                .map(EffectPair::fromEffect)
                .toArray(EffectPair[]::new);
    }

    public record EffectPair(@Nullable CraftingEffectCondition requirement, CraftingEffectOutcome outcome) {
        public EffectPair {
            if (requirement == null) {
                requirement = CraftingEffectCondition.any;
            }
        }

        public static EffectPair fromEffect(CraftingEffect craftingEffect) {
            if (craftingEffect.getOutcomes().length > 1) {
                return new EffectPair(craftingEffect.getRequirement(), new ApplyListOutcome(Arrays.stream(craftingEffect.getOutcomes())
                        .map(outcome -> new EffectPair(craftingEffect.getRequirement(), outcome))
                        .toArray(EffectPair[]::new)));
            }
            return new EffectPair(craftingEffect.getRequirement(), craftingEffect.getOutcomes()[0]);
        }
    }
}
