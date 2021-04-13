package se.mickelus.tetra.craftingeffect.outcome;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.util.CastOptional;

import java.util.Map;

public class RemoveImprovementOutcome implements CraftingEffectOutcome {
    String[] improvements;

    @Override
    public boolean apply(ItemStack upgradedStack, String slot, boolean isReplacing, PlayerEntity player, ItemStack[] preMaterials,
            Map<ToolType, Integer> tools, World world, BlockPos pos, BlockState blockState, boolean consumeResources, ItemStack[] postMaterials) {
        return CastOptional.cast(upgradedStack.getItem(), IModularItem.class)
                .map(item -> item.getModuleFromSlot(upgradedStack, slot))
                .flatMap(module -> CastOptional.cast(module, ItemModuleMajor.class))
                .map(module -> {
                    boolean result = false;
                    for (String improvement: improvements) {
                        if (module.getImprovementLevel(upgradedStack, improvement) != -1) {
                            module.removeImprovement(upgradedStack, improvement);
                            result = true;
                        }
                    }
                    return result;
                })
                .orElse(false);
    }
}
