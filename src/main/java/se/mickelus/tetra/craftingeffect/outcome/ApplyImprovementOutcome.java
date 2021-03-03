package se.mickelus.tetra.craftingeffect.outcome;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.util.CastOptional;

import java.util.Map;

public class ApplyImprovementOutcome implements CraftingEffectOutcome {
    Map<String, Integer> improvements;

    @Override
    public boolean apply(ItemStack upgradedStack, String slot, boolean isReplacing, PlayerEntity player, ItemStack[] preMaterials,
            Map<ToolType, Integer> tools, World world, BlockPos pos, BlockState blockState, boolean consumeResources, ItemStack[] postMaterials) {
        return CastOptional.cast(upgradedStack.getItem(), ModularItem.class)
                .map(item -> item.getModuleFromSlot(upgradedStack, slot))
                .flatMap(module -> CastOptional.cast(module, ItemModuleMajor.class))
                .map(module -> {
                    boolean result = false;
                    for (Map.Entry<String, Integer> improvement: improvements.entrySet()){
                        if (module.acceptsImprovementLevel(improvement.getKey(), improvement.getValue())) {
                            module.addImprovement(upgradedStack, improvement.getKey(), improvement.getValue());
                            result = true;
                        }
                    }
                    return result;
                })
                .orElse(false);
    }
}
