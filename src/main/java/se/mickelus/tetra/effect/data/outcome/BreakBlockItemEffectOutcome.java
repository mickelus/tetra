package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.condition.FixedItemEffectCondition;
import se.mickelus.tetra.effect.data.condition.ItemEffectCondition;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class BreakBlockItemEffectOutcome extends ItemEffectOutcome {
    VectorProvider position;
    ItemEffectCondition harvest = new FixedItemEffectCondition(false);

    @Override
    public boolean perform(ItemEffectContext context) {
        BlockPos target = position.getBlockPos(context);
        if (context.getUsingEntity() instanceof Player player) {
            return EffectHelper.breakBlock(context.getLevel(), player, context.getUsedItemStack(), target, context.getLevel().getBlockState(target),
                    harvest.test(context), false);
        }
        return false;
    }
}
