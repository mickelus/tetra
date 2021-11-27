package se.mickelus.tetra.craftingeffect.outcome;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import java.util.Map;

public class MaterialReductionOutcome implements CraftingEffectOutcome {
    float probability;

    @Override
    public boolean apply(ItemStack upgradedStack, String slot, boolean isReplacing, PlayerEntity player, ItemStack[] preMaterials,
            Map<ToolType, Integer> tools, World world, BlockPos pos, BlockState blockState, boolean consumeResources, ItemStack[] postMaterials) {
        if (consumeResources
                && !preMaterials[0].isEmpty()
                && (ItemStack.areItemsEqual(preMaterials[0], postMaterials[0]) || postMaterials[0].isEmpty())
                && preMaterials[0].getCount() > postMaterials[0].getCount() + 1) {
            if (world.getRandom().nextFloat() < probability) {
                if (ItemStack.areItemsEqual(preMaterials[0], postMaterials[0])) {
                    postMaterials[0].setCount(postMaterials[0].getCount() + 1);
                } else {
                    ItemStack clone = preMaterials[0].copy();
                    clone.setCount(1);
                    postMaterials[0] = clone;
                }
                return true;
            }
        }
        return false;
    }
}
