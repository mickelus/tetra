package se.mickelus.tetra.effect.data.outcome;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import se.mickelus.tetra.effect.data.ItemEffectContext;
import se.mickelus.tetra.effect.data.provider.entity.EntityProvider;
import se.mickelus.tetra.effect.data.provider.vector.VectorProvider;

public class ImitateItemEffectOutcome extends ItemEffectOutcome {
    ItemStack itemStack;
    ImitateType effect;
    EntityProvider targetEntity;
    EntityProvider user;
    VectorProvider targetPosition;

    @Override
    public boolean perform(ItemEffectContext context) {
        switch (effect) {
            case swing:
                return swing(context);
            case breakBlockStart:
                return breakBlockStart(context);
            case hurtEnemy:
                return hurtEnemy(context);
            case mineBlock:
                return mineBlock(context);
            case leftClickEntity:
                return leftClickEntity(context);
            case finishUsing:
                return finishUsing(context);
        }
        return false;
    }

    private boolean mineBlock(ItemEffectContext context) {
        if (user.getEntity(context) instanceof Player player) {
            BlockPos targetPos = targetPosition.getBlockPos(context);
            if (targetPos != null) {
                BlockState targetState = context.getLevel().getBlockState(targetPos);
                itemStack.mineBlock(context.getLevel(), targetState, targetPos, player);
                return true;
            }
        }
        return false;
    }

    private boolean swing(ItemEffectContext context) {
        if (user.getEntity(context) instanceof LivingEntity livingEntity) {
            return context.getUsedItemStack().onEntitySwing(livingEntity, resolveSwingHand(livingEntity, context.getUsedItemStack()));
        }
        return false;
    }

    private static InteractionHand resolveSwingHand(LivingEntity entity, ItemStack stack) {
        if (ItemStack.isSameItemSameComponents(entity.getItemInHand(InteractionHand.MAIN_HAND), stack)) {
            return InteractionHand.MAIN_HAND;
        }
        if (ItemStack.isSameItemSameComponents(entity.getItemInHand(InteractionHand.OFF_HAND), stack)) {
            return InteractionHand.OFF_HAND;
        }
        return InteractionHand.MAIN_HAND;
    }

    private boolean breakBlockStart(ItemEffectContext context) {
        if (user.getEntity(context) instanceof Player player) {
            BlockPos targetPos = targetPosition.getBlockPos(context);
            if (targetPos != null) {
                // The old pre-break stack hook was removed in 1.21.x.
                return false;
            }
        }
        return false;
    }

    private boolean hurtEnemy(ItemEffectContext context) {
        if (targetEntity.getEntity(context) instanceof LivingEntity livingTarget && user.getEntity(context) instanceof Player playerAttacker) {
            itemStack.hurtEnemy(livingTarget, playerAttacker);
            return true;
        }
        return false;
    }

    private boolean leftClickEntity(ItemEffectContext context) {
        if (targetEntity.getEntity(context) instanceof LivingEntity livingTarget && user.getEntity(context) instanceof Player player) {
            return itemStack.getItem().onLeftClickEntity(itemStack, player, livingTarget);
        }
        return false;
    }

    private boolean finishUsing(ItemEffectContext context) {
        if (user.getEntity(context) instanceof Player player) {
            itemStack.finishUsingItem(context.getLevel(), player);
            return true;
        }
        return false;
    }

    enum ImitateType {
        swing,
        leftClickEntity,
        hurtEnemy,
        breakBlockStart,
        mineBlock,
        finishUsing
    }
}
