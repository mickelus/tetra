package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.condition.FixedItemEffectCondition;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class BreakBlockItemEffectOutcome extends ItemEffectOutcome {
    VectorProvider position;
    ItemEffectCondition harvest = new FixedItemEffectCondition(false);
    EntityProvider breaker;

    @Override
    public boolean perform(ItemEffectContext context) {
        if (breaker.getEntity(context) instanceof Player player) {
            BlockPos target = position.getBlockPos(context);
            if (target != null) {
                BlockState blockState = context.getLevel().getBlockState(target);
                boolean success = EffectHelper.breakBlock(context.getLevel(), player, context.getUsedItemStack(), target, blockState,
                        harvest.test(context), false);
                if (success) {
                    EffectHelper.sendEventToPlayer((ServerPlayer) player, 2001, target, Block.getId(blockState));
                }
                return success;
            }
        }
        return false;
    }
}
