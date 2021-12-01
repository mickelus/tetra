package se.mickelus.tetra.craftingeffect.condition;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.module.data.ToolData;

import java.util.Map;

public class ToolCondition implements CraftingEffectCondition {
    ToolData tools;

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] materials, Map<ToolType, Integer> tools, Level world, BlockPos pos, BlockState blockState) {
        for (Map.Entry<ToolType, Float> req: this.tools.levelMap.entrySet()) {
            if (!tools.containsKey(req.getKey()) || tools.get(req.getKey()) < req.getValue()) {
                return false;
            }
        }
        return true;
    }
}
