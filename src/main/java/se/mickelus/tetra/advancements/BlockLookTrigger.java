package se.mickelus.tetra.advancements;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntityMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import se.mickelus.tetra.blocks.PropertyMatcher;
import se.mickelus.tetra.data.DataHandler;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BlockLookTrigger extends GenericTrigger<BlockLookTrigger.Instance> {
    private Cache<UUID, IBlockState> stateCache;

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
        if (event.player.ticksExisted % 20 == 0 && !event.player.world.isRemote) {
            event.player.world.profiler.startSection("lookTrigger");
            RayTraceResult result = ForgeHooks.rayTraceEyes(event.player, event.player.getEntityAttribute(PlayerEntity.REACH_DISTANCE).getAttributeValue());

            if (result != null && RayTraceResult.Type.BLOCK.equals(result.typeOfHit)) {
                IBlockState currentState = event.player.world.getBlockState(result.getBlockPos());

                if (!currentState.equals(stateCache.getIfPresent(event.player.getUniqueID()))) {
                    trigger((PlayerEntityMP) event.player, currentState);
                    stateCache.put(event.player.getUniqueID(), currentState);
                }
            } else {
                stateCache.invalidate(event.player.getUniqueID());
            }
            event.player.world.profiler.endSection();
        }
    }

    public static Instance deserialize(JsonObject json) {
        return DataHandler.instance.gson.fromJson(json, Instance.class);
    }

    public void trigger(PlayerEntityMP player, IBlockState state) {
        fulfillCriterion(player.getAdvancements(), instance -> instance.test(state));
    }

    public static class Instance extends AbstractCriterionInstance {
        private PropertyMatcher block = null;

        public Instance() {
            super(instance.getId());
        }

        public boolean test(IBlockState state) {
            return block != null && block.test(state);
        }
    }
}
