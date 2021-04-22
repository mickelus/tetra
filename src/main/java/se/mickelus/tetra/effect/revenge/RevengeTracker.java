package se.mickelus.tetra.effect.revenge;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.network.PacketHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class RevengeTracker {
    private static final Logger logger = LogManager.getLogger();

    private static Cache<Integer, Collection<Integer>> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    private static int getIdentifier(Entity entity) {
        return entity.world.isRemote ? -entity.getEntityId() : entity.getEntityId();
    }

    public static boolean canRevenge(LivingEntity entity) {
        return Stream.of(entity.getHeldItemMainhand(), entity.getHeldItemOffhand())
                .filter(itemStack -> itemStack.getItem() instanceof IModularItem)
                .anyMatch(itemStack -> canRevenge((IModularItem) itemStack.getItem(), itemStack));
    }


    public static boolean canRevenge(IModularItem item, ItemStack itemStack) {
        return item.getEffectLevel(itemStack, ItemEffect.abilityRevenge) > 0;
    }

    public static boolean canRevenge(Entity entity, Entity enemy) {
        return Optional.ofNullable(cache.getIfPresent(getIdentifier(entity)))
                .map(enemies -> enemies.contains(enemy.getEntityId()))
                .orElse(false);
    }

    public static void onAttackEntity(LivingAttackEvent event) {
        Entity entity = event.getEntity();
        if (!event.getEntity().getEntityWorld().isRemote() && EntityType.PLAYER.equals(entity.getType())) {
            Entity enemy = event.getSource().getTrueSource();
            if (enemy != null) {
                addEnemy(entity, enemy);

                if (entity instanceof ServerPlayerEntity) {
                    PacketHandler.sendTo(new AddRevengePacket(enemy), (ServerPlayerEntity) entity);
                } else {
                    logger.warn("Unable to sync revenge state, server entity of type player is of other heritage. This should not happen");
                }
            }
        }
    }

    /**
     * Removes the enemy as a revenge target for the given player, sync to the client of the player
     * @param entity
     * @param enemy
     */
    public static void removeEnemySynced(ServerPlayerEntity entity, Entity enemy) {
        removeEnemy(entity, enemy.getEntityId());
        PacketHandler.sendTo(new RemoveRevengePacket(enemy), entity);
    }

    public static void removeEnemy(Entity entity, Entity enemy) {
        removeEnemy(entity, enemy.getEntityId());
    }

    public static void removeEnemy(Entity entity, int enemyId) {
        Optional.ofNullable(cache.getIfPresent(getIdentifier(entity)))
                .ifPresent(enemies -> enemies.remove(enemyId));
    }

    public static void addEnemy(Entity entity, Entity enemy) {
        addEnemy(entity, enemy.getEntityId());
    }

    public static void addEnemy(Entity entity, int enemyId) {
        try {
            cache.get(getIdentifier(entity), HashSet::new).add(enemyId);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
