package se.mickelus.tetra.craftingeffect.outcome;

import com.google.common.collect.Streams;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.util.LazyOptional;
import se.mickelus.tetra.craftingeffect.CraftingEffect;
import se.mickelus.tetra.craftingeffect.CraftingEffectRegistry;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;
import se.mickelus.tetra.util.StreamHelper;

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
    CraftingEffect.EffectPair[] effects = new CraftingEffect.EffectPair[0];
    boolean random = false;
    int count = Integer.MAX_VALUE;

    LazyOptional<CraftingEffect.EffectPair[]> resolvedReferences = LazyOptional.of(() -> resolveReferences(references));

    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] preMaterials,
            Map<ToolAction, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos pos, BlockState blockState, boolean consumeResources,
            ItemStack[] postMaterials) {
        Collector<CraftingEffect.EffectPair, ?, List<CraftingEffect.EffectPair>> collector = random
                ? StreamHelper.toShuffledList()
                : Collectors.toUnmodifiableList();

        List<CraftingEffect.EffectPair> applicableOutcomes = Streams.concat(Arrays.stream(effects), resolvedReferences.lazyMap(Arrays::stream).orElseGet(Stream::empty))
                .filter(outcome -> outcome.requirement().test(unlockedEffects, upgradedStack, slot, isReplacing, player, preMaterials, tools, schematic,
                        world, pos, blockState))
                .collect(collector);

        for (int i = 0; i < applicableOutcomes.size() & i < count; i++) {
            applicableOutcomes.get(i).outcome().apply(unlockedEffects, upgradedStack, slot, isReplacing, player, preMaterials, tools, world, schematic, pos, blockState, consumeResources, postMaterials);
        }
        return !applicableOutcomes.isEmpty();
    }

    private static CraftingEffect.EffectPair[] resolveReferences(ResourceLocation[] references) {
        return Arrays.stream(CraftingEffectRegistry.getEffects(references))
                .map(CraftingEffect.EffectPair::fromEffect)
                .flatMap(Arrays::stream)
                .toArray(CraftingEffect.EffectPair[]::new);
    }
}
