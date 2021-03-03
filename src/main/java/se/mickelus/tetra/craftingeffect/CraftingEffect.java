package se.mickelus.tetra.craftingeffect;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.craftingeffect.condition.CraftingEffectCondition;
import se.mickelus.tetra.craftingeffect.outcome.CraftingEffectOutcome;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public class CraftingEffect {
    public boolean replace = false;
    CraftingEffectCondition[] conditions = new CraftingEffectCondition[0];
    CraftingEffectOutcome[] outcomes = new CraftingEffectOutcome[0];
    CraftingProperties properties = new CraftingProperties();

    public boolean isApplicable(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, PlayerEntity player,
            ItemStack[] materials, Map<ToolType, Integer> tools, World world, BlockPos pos, BlockState blockState) {
        return Arrays.stream(conditions)
                .allMatch(condition -> condition.test(unlocks, upgradedStack, slot, isReplacing, player, materials, tools, world, pos, blockState));
    }

    public boolean applyOutcomes(ItemStack upgradedStack, String slot, boolean isReplacing, PlayerEntity player, ItemStack[] preMaterials,
            ItemStack[] postMaterials, Map<ToolType, Integer> tools, World world, BlockPos pos, BlockState blockState, boolean consumeResources) {
        boolean success = false;
        for (CraftingEffectOutcome outcome: outcomes) {
            if (outcome.apply(upgradedStack, slot, isReplacing, player, preMaterials, tools, world, pos, blockState, consumeResources, postMaterials)) {
                success = true;
            }
        }

        return success;
    }

    public static void copyFields(CraftingEffect from, CraftingEffect to) {
        to.conditions = Stream.concat(Arrays.stream(to.conditions), Arrays.stream(from.conditions)).toArray(CraftingEffectCondition[]::new);
        to.outcomes = Stream.concat(Arrays.stream(to.outcomes), Arrays.stream(from.outcomes)).toArray(CraftingEffectOutcome[]::new);

        to.properties = CraftingProperties.merge(from.properties, to.properties);
    }
}
