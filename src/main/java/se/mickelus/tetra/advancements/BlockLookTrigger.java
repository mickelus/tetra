package se.mickelus.tetra.advancements;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
        if (TickEvent.Phase.START == event.phase && event.player.tickCount % 20 == 0 && !event.player.level.isClientSide) {
            event.player.level.getProfiler().push("lookTrigger");
            Vec3 playerPosition = event.player.getEyePosition(0);

            float lookDistance = 5; // event.player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue()
            Vec3 lookingPosition = event.player.getLookAngle().scale(lookDistance).add(playerPosition);

            BlockHitResult result = event.player.level.clip(new ClipContext(playerPosition, lookingPosition,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, event.player));


            if (!HitResult.Type.MISS.equals(result.getType())) {
                BlockState currentState = event.player.level.getBlockState(new BlockPos(result.getBlockPos()));

                if (!currentState.equals(stateCache.getIfPresent(event.player.getUUID()))) {
                    trigger((ServerPlayer) event.player, currentState);
                    stateCache.put(event.player.getUUID(), currentState);
                }
            } else {
                stateCache.invalidate(event.player.getUUID());
            }
            event.player.level.getProfiler().pop();
        }
    }

    public static Instance deserialize(JsonObject json, EntityPredicate.Composite entityPredicate, DeserializationContext conditionsParser) {
        PropertyMatcher propertyMatcher = null;
        if (json.has("block")) {
            propertyMatcher = PropertyMatcher.deserialize(json.get("block"));
        }

        return new Instance(entityPredicate, propertyMatcher);
    }

    public void trigger(ServerPlayer player, BlockState state) {
        fulfillCriterion(player, instance -> instance.test(state));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private PropertyMatcher block = null;

        public Instance(EntityPredicate.Composite playerCondition, PropertyMatcher propertyMatcher) {
            super(instance.getId(), playerCondition);

            this.block = propertyMatcher;
        }

        public boolean test(BlockState state) {
            return block != null && block.test(state);
        }
    }
}
