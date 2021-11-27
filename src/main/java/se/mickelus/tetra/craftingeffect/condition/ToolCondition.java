package se.mickelus.tetra.craftingeffect.condition;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.module.data.ToolData;

import java.util.Map;

public class ToolCondition implements CraftingEffectCondition {
    ToolData tools;

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, PlayerEntity player,
            ItemStack[] materials, Map<ToolType, Integer> tools, World world, BlockPos pos, BlockState blockState) {
        for (Map.Entry<ToolType, Float> req: this.tools.levelMap.entrySet()) {
            if (!tools.containsKey(req.getKey()) || tools.get(req.getKey()) < req.getValue()) {
                return false;
            }
        }
        return true;
    }
}
