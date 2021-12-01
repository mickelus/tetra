package se.mickelus.tetra.craftingeffect;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.craftingeffect.condition.CraftingEffectCondition;
import se.mickelus.tetra.craftingeffect.outcome.CraftingEffectOutcome;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public class CraftingEffect {
    public boolean replace = false;
    CraftingEffectCondition[] requirements = new CraftingEffectCondition[0];
    CraftingEffectOutcome[] outcomes = new CraftingEffectOutcome[0];
    CraftingProperties properties = new CraftingProperties();

    public boolean isApplicable(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] materials, Map<ToolType, Integer> tools, Level world, BlockPos pos, BlockState blockState) {
        return Arrays.stream(requirements)
                .allMatch(condition -> condition.test(unlocks, upgradedStack, slot, isReplacing, player, materials, tools, world, pos, blockState));
    }

    public boolean applyOutcomes(ItemStack upgradedStack, String slot, boolean isReplacing, Player player, ItemStack[] preMaterials,
            ItemStack[] postMaterials, Map<ToolType, Integer> tools, Level world, BlockPos pos, BlockState blockState, boolean consumeResources) {
        boolean success = false;
        for (CraftingEffectOutcome outcome: outcomes) {
            if (outcome.apply(upgradedStack, slot, isReplacing, player, preMaterials, tools, world, pos, blockState, consumeResources, postMaterials)) {
                success = true;
            }
        }

        return success;
    }

    public static void copyFields(CraftingEffect from, CraftingEffect to) {
        to.requirements = Stream.concat(Arrays.stream(to.requirements), Arrays.stream(from.requirements)).toArray(CraftingEffectCondition[]::new);
        to.outcomes = Stream.concat(Arrays.stream(to.outcomes), Arrays.stream(from.outcomes)).toArray(CraftingEffectOutcome[]::new);

        to.properties = CraftingProperties.merge(from.properties, to.properties);
    }
}
