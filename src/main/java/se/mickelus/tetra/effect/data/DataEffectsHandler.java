package se.mickelus.tetra.effect.data;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import se.mickelus.tetra.data.ItemEffectStore;
import se.mickelus.tetra.items.modular.IModularItem;

public class DataEffectsHandler {
    public static void applyOnHitEffects(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel serverLevel) {
            ItemEffectContext context = new ItemEffectContext(attacker, itemStack, serverLevel)
                    .withEntities(ImmutableMap.of("attacker", attacker, "target", target));
            ((IModularItem) itemStack.getItem()).getEffects(itemStack).stream()
                    .flatMap(effect -> ItemEffectStore.onHitEffects.get(effect).stream())
                    .forEach(effect -> prepareDataAndPerformOutcome(effect, context));
        }
    }

    public static void applyMineBlockEffects(ItemStack itemStack, LivingEntity breaker, BlockPos targetPos) {
        if (breaker.level() instanceof ServerLevel serverLevel) {
            ItemEffectContext context = new ItemEffectContext(breaker, itemStack, serverLevel)
                    .withEntities(ImmutableMap.of("miner", breaker))
                    .withVectors(ImmutableMap.of("target", Vec3.atLowerCornerOf(targetPos)));
            ((IModularItem) itemStack.getItem()).getEffects(itemStack).stream()
                    .flatMap(effect -> ItemEffectStore.onMineBlockEffects.get(effect).stream())
                    .forEach(effect -> prepareDataAndPerformOutcome(effect, context));
        }
    }

    public static void applyBreakBlockEffects(ItemStack itemStack, LivingEntity breaker, BlockPos targetPos) {
        if (breaker.level() instanceof ServerLevel serverLevel) {
            ItemEffectContext context = new ItemEffectContext(breaker, itemStack, serverLevel)
                    .withEntities(ImmutableMap.of("breaker", breaker))
                    .withVectors(ImmutableMap.of("target", Vec3.atLowerCornerOf(targetPos)));
            ((IModularItem) itemStack.getItem()).getEffects(itemStack).stream()
                    .flatMap(effect -> ItemEffectStore.onBreakBlockEffects.get(effect).stream())
                    .forEach(effect -> prepareDataAndPerformOutcome(effect, context));
        }
    }

    private static void prepareDataAndPerformOutcome(ItemEffectData effect, ItemEffectContext context) {
        context = context.withMergedNumbers(ItemEffectData.calculateNumbers(effect.data, context));
        context = context.withMergedVectors(ItemEffectData.calculateVectors(effect.data, context));
        context = context.withMergedEntities(ItemEffectData.calculateEntities(effect.data, context));
        if (effect.condition == null || effect.condition.test(context)) {
            effect.outcome.perform(context);
        }
    }
}
