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

    // todo 1.20 verify: material treatises occasionally returns mats when crafting modules
    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player, ItemStack[] preMaterials,
            Map<ToolAction, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos pos, BlockState blockState, boolean consumeResources, ItemStack[] postMaterials) {
        if (consumeResources
                && !preMaterials[0].isEmpty()
                && (ItemStack.isSameItem(preMaterials[0], postMaterials[0]) || postMaterials[0].isEmpty())
                && preMaterials[0].getCount() > postMaterials[0].getCount() + 1) {
            if (world.getRandom().nextFloat() < probability) {
                if (ItemStack.isSameItem(preMaterials[0], postMaterials[0])) {
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
