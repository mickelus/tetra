package se.mickelus.tetra.advancements;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.blocks.PropertyMatcher;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BlockLookTrigger extends GenericTrigger<BlockLookTrigger.Instance> {
    private Cache<UUID, BlockState> stateCache;

    public static final BlockLookTrigger instance = new BlockLookTrigger();

    public BlockLookTrigger() {
        super("tetra:block_look", BlockLookTrigger::deserialize);
        stateCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (TickEvent.Phase.START == event.phase && event.player.ticksExisted % 20 == 0 && !event.player.world.isRemote) {
            event.player.world.getProfiler().startSection("lookTrigger");
            Vector3d playerPosition = event.player.getEyePosition(0);

            float lookDistance = 5; // event.player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue()
            Vector3d lookingPosition = event.player.getLookVec().scale(lookDistance).add(playerPosition);

            BlockRayTraceResult result = event.player.world.rayTraceBlocks(new RayTraceContext(playerPosition, lookingPosition,
                    RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, event.player));


            if (!RayTraceResult.Type.MISS.equals(result.getType())) {
                BlockState currentState = event.player.world.getBlockState(new BlockPos(result.getPos()));

                if (!currentState.equals(stateCache.getIfPresent(event.player.getUniqueID()))) {
                    trigger((ServerPlayerEntity) event.player, currentState);
                    stateCache.put(event.player.getUniqueID(), currentState);
                }
            } else {
                stateCache.invalidate(event.player.getUniqueID());
            }
            event.player.world.getProfiler().endSection();
        }
    }

    public static Instance deserialize(JsonObject json, EntityPredicate.AndPredicate entityPredicate, ConditionArrayParser conditionsParser) {
        PropertyMatcher propertyMatcher = null;
        if (json.has("block")) {
            propertyMatcher = PropertyMatcher.deserialize(json.get("block"));
        }

        return new Instance(entityPredicate, propertyMatcher);
    }

    public void trigger(ServerPlayerEntity player, BlockState state) {
        fulfillCriterion(player, instance -> instance.test(state));
    }

    public static class Instance extends CriterionInstance {
        private PropertyMatcher block = null;

        public Instance(EntityPredicate.AndPredicate playerCondition, PropertyMatcher propertyMatcher) {
            super(instance.getId(), playerCondition);

            this.block = propertyMatcher;
        }

        public boolean test(BlockState state) {
            return block != null && block.test(state);
        }
    }
}
