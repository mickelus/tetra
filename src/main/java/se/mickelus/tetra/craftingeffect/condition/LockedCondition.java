package se.mickelus.tetra.craftingeffect.condition;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolType;

import java.util.Arrays;
import java.util.Map;

public class LockedCondition implements CraftingEffectCondition {
    ResourceLocation key;

    @Override
    public boolean test(ResourceLocation[] unlocks, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] materials, Map<ToolType, Integer> tools, Level world, BlockPos pos, BlockState blockState) {
        return key != null && Arrays.stream(unlocks).anyMatch(key::equals);
    }
}
