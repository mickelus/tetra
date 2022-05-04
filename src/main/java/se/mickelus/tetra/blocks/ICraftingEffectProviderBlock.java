package se.mickelus.tetra.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ICraftingEffectProviderBlock {
    default boolean canUnlockCraftingEffects(Level world, BlockPos pos, BlockPos targetPos) {
        return false;
    }

    default ResourceLocation[] getCraftingEffects(Level world, BlockPos pos, BlockState blockState) {
        return new ResourceLocation[0];
    }
}
