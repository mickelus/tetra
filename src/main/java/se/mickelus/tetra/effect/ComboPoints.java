package se.mickelus.tetra.effect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import se.mickelus.tetra.items.modular.IModularItem;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ComboPoints {
    private static Cache<Integer, Integer> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    private static int getIdentifier(Entity entity) {
        return entity.level.isClientSide ? -entity.getId() : entity.getId();
    }

    public static void increment(Entity entity) {
        try {
            int identifier = getIdentifier(entity);
            int points = Math.min(5, cache.get(identifier, () -> 0) + 1);
            cache.put(identifier, points);
        } catch(ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static int get(Entity entity) {
        try {
            return cache.get(getIdentifier(entity), () -> 0);
        } catch(ExecutionException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static int getAndReset(Entity entity) {
        try {
            int points = cache.get(getIdentifier(entity), () -> 0);
            reset(entity);
            return points;
        } catch(ExecutionException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void reset(Entity entity) {
        cache.invalidate(getIdentifier(entity));
    }

    public static boolean canSpend(LivingEntity entity) {
        return Stream.of(entity.getMainHandItem(), entity.getOffhandItem())
                .filter(itemStack -> itemStack.getItem() instanceof IModularItem)
                .anyMatch(itemStack -> canSpend((IModularItem) itemStack.getItem(), itemStack));
    }


    public static boolean canSpend(IModularItem item, ItemStack itemStack) {
        return item.getEffectLevel(itemStack, ItemEffect.abilityCombo) > 0;
    }

    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getTarget().isAttackable()
                && canSpend(event.getPlayer())
                && event.getPlayer().getAttackStrengthScale(0) > 0.9) {
            increment(event.getPlayer());
        }
    }
}
