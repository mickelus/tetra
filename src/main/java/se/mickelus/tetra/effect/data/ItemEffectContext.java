package se.mickelus.tetra.effect.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public record ItemEffectContext(
        LivingEntity usingEntity,
        ItemStack usedItemStack,
        Level level,
        @Nullable LivingEntity targetEntity,
        @Nullable BlockPos targetPos,
        @Nullable BlockState targetState
) {

}
