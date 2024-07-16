package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.effect.data.ItemEffectContext;

public class ImitateItemEffectOutcome extends ItemEffectOutcome {
    ItemStack itemStack;
    ImitateType type;

    @Override
    public boolean perform(ItemEffectContext context) {
        switch (type) {
            case swing:
                return itemStack.onEntitySwing(context.getUsingEntity());
            case breakBlockStart:
                if (context.getUsingEntity() instanceof Player player) {
                    return itemStack.onBlockStartBreak(context.getTargetPos(), player);
                }
                return false;
            case hurtEnemy:
                if (context.getTargetEntity() instanceof LivingEntity livingTarget && context.getUsingEntity() instanceof Player player) {
                    itemStack.hurtEnemy(livingTarget, player);
                    return true;
                }
                return false;
            case mineBlock:
                if (context.getTargetState() != null && context.getTargetPos() != null
                        && context.getUsingEntity() instanceof Player player) {
                    itemStack.mineBlock(context.getLevel(), context.getTargetState(), context.getTargetPos(), player);
                    return true;
                }
                return false;
            case leftClickEntity:
                if (context.getTargetEntity() != null && context.getUsingEntity() instanceof Player player) {
                    itemStack.getItem().onLeftClickEntity(itemStack, player, context.getTargetEntity());
                    return true;
                }
                return false;
            case finishUsing:
                itemStack.finishUsingItem(context.getLevel(), context.getUsingEntity());
                return true;
        }
        return false;
    }

    enum ImitateType {
        swing,
        breakBlockStart,
        hurtEnemy,
        mineBlock,
        leftClickEntity,
        finishUsing
    }
}
