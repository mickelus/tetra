package se.mickelus.tetra.advancements;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.data.DataHandler;

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

    // todo 1.14: validate that this still works
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.ticksExisted % 20 == 0 && !event.player.world.isRemote) {
            event.player.world.getProfiler().startSection("lookTrigger");
            Vec3d playerPosition = event.player.getPositionVec().add(event.player.getEyePosition(0));
            Vec3d lookingPosition = playerPosition.add(event.player.getLookVec()
                    .scale(event.player.getAttribute(PlayerEntity.REACH_DISTANCE).getValue()));

            BlockRayTraceResult result = event.player.world.rayTraceBlocks(new RayTraceContext(playerPosition, lookingPosition,
                    RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, event.player));


            if (!RayTraceResult.Type.MISS.equals(result.getType())) {
                BlockState currentState = event.player.world.getBlockState(new BlockPos(result.getHitVec()));

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

    public static Instance deserialize(JsonObject json) {
        return DataHandler.instance.gson.fromJson(json, Instance.class);
    }

    public void trigger(ServerPlayerEntity player, BlockState state) {
        fulfillCriterion(player.getAdvancements(), instance -> instance.test(state));
    }

    public static class Instance extends CriterionInstance {
        private PropertyMatcher block = null;

        public Instance() {
            super(instance.getId());
        }

        public boolean test(BlockState state) {
            return block != null && block.test(state);
        }
    }
}
