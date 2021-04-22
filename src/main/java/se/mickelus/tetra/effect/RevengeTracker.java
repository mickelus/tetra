package se.mickelus.tetra.effect;

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

    public static boolean canRevenge(Entity attacker, Entity target) {
        return Optional.ofNullable(cache.getIfPresent(getIdentifier(attacker)))
                .map(enemies -> enemies.contains(target.getEntityId()))
                .orElse(false);
    }

    public static void remove(Entity attacker, Entity target) {
        Optional.ofNullable(cache.getIfPresent(getIdentifier(attacker)))
                .ifPresent(enemies -> enemies.remove(target.getEntityId()));
    }

    public static void onAttackEntity(LivingAttackEvent event) {
        Entity defender = event.getEntity();
        if (!event.getEntity().getEntityWorld().isRemote() && EntityType.PLAYER.equals(defender.getType())) {
            Entity attacker = event.getSource().getTrueSource();
            if (attacker != null) {
                addEnemy(attacker, defender);

                if (defender instanceof ServerPlayerEntity) {
                    PacketHandler.sendTo(new RevengeTrackerPacket(attacker), (ServerPlayerEntity) defender);
                } else {
                    logger.warn("Unable to sync revenge state, server entity of type player is of other heritage. This should not happen");
                }
            }
        }
    }

    public static void addEnemy(Entity attacker, Entity defender) {
        addEnemy(attacker.getEntityId(), defender);
    }

    public static void addEnemy(int attackerId, Entity defender) {
        try {
            cache.get(getIdentifier(defender), HashSet::new).add(attackerId);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
