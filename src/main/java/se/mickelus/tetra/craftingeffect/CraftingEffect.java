package se.mickelus.tetra.craftingeffect;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.craftingeffect.condition.CraftingEffectCondition;
import se.mickelus.tetra.craftingeffect.outcome.CraftingEffectOutcome;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class CraftingEffect {
    public boolean replace = false;
    public boolean active = true;
    CraftingEffectCondition requirement = CraftingEffectCondition.any;
    CraftingEffectOutcome[] outcomes = new CraftingEffectOutcome[0];
    CraftingProperties properties = new CraftingProperties();

    public static void copyFields(CraftingEffect from, CraftingEffect to) {
        if (!from.requirement.equals(CraftingEffectCondition.any)) {
            to.requirement = from.requirement;
        }
        to.outcomes = Stream.concat(Arrays.stream(to.outcomes), Arrays.stream(from.outcomes)).toArray(CraftingEffectOutcome[]::new);

        to.properties = CraftingProperties.merge(from.properties, to.properties);
    }

    public boolean isApplicable(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] materials, Map<ToolAction, Integer> tools, UpgradeSchematic schematic, Level world, BlockPos pos, BlockState blockState) {
        return requirement.test(unlocks, upgradedStack, slot, isReplacing, player, materials, tools, schematic, world, pos, blockState);
    }

    public boolean applyOutcomes(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] preMaterials, ItemStack[] postMaterials, Map<ToolAction, Integer> tools, Level world, UpgradeSchematic schematic,
            BlockPos pos, BlockState blockState, boolean consumeResources, float severity) {
        boolean success = false;
        for (CraftingEffectOutcome outcome : outcomes) {
            if (outcome.apply(unlockedEffects, upgradedStack, slot, isReplacing, player, preMaterials, tools, world, schematic, pos, blockState, consumeResources, postMaterials, severity)) {
                success = true;
            }
        }

        return success;
    }

    public CraftingEffectCondition getRequirement() {
        return requirement;
    }

    public CraftingEffectOutcome[] getOutcomes() {
        return outcomes;
    }
}
