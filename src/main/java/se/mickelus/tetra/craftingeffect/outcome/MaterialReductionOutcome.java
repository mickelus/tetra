package se.mickelus.tetra.craftingeffect.outcome;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
public class MaterialReductionOutcome implements CraftingEffectOutcome {
    float probability;
    float diminishingMultiplier = 0.5f;

    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] preMaterials, Map<ToolAction, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos pos, BlockState blockState,
            boolean consumeResources,
            ItemStack[] postMaterials, float severity) {
        if (consumeResources
                && !preMaterials[0].isEmpty()
                && (ItemStack.isSameItem(preMaterials[0], postMaterials[0]) || postMaterials[0].isEmpty())
                && preMaterials[0].getCount() > postMaterials[0].getCount()) {
            int usedCount = preMaterials[0].getCount() - postMaterials[0].getCount();
            float currentProbability = probability;
            boolean success = false;
            ItemStack currentMaterialStack = getCurrentMaterialStack(postMaterials, preMaterials);

            for (int i = 0; i < usedCount; i++) {
                if (world.getRandom().nextFloat() < currentProbability) {
                    currentMaterialStack.setCount(Math.min(currentMaterialStack.getCount() + 1, preMaterials[0].getCount()));
                    currentProbability *= diminishingMultiplier;
                    success = true;
                }
            }

            if (success) {
                postMaterials[0] = currentMaterialStack;
                return true;
            }
        }
        return false;
    }

    private ItemStack getCurrentMaterialStack(ItemStack[] postMaterials, ItemStack[] preMaterials) {
        if (!postMaterials[0].isEmpty()) {
            return postMaterials[0].copy();
        } else {
            ItemStack clone = preMaterials[0].copy();
            clone.setCount(0);
            return clone;
        }
    }
}
