package se.mickelus.tetra.effect;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.util.CastOptional;

import java.util.Optional;

public class PiercingEffect {
    public static void pierceBlocks(ModularItem item, ItemStack itemStack, int pierceAmount, ServerWorld world, BlockState state, BlockPos pos, LivingEntity entity) {
        PlayerEntity player = CastOptional.cast(entity, PlayerEntity.class).orElse(null);

        if (pierceAmount > 0) {
            double critMultiplier = CritEffect.rollMultiplier(entity.getRNG(), item, itemStack);
            if (critMultiplier != 1) {
                pierceAmount *= critMultiplier;
                world.spawnParticle(ParticleTypes.ENCHANTED_HIT, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, 15, 0.2D, 0.2D, 0.2D, 0.0D);
            }

            Vector3d entityPosition = entity.getEyePosition(0);
            double lookDistance = Optional.ofNullable(entity.getAttribute(ForgeMod.REACH_DISTANCE.get()))
                    .map(ModifiableAttributeInstance::getValue)
                    .orElse(5d);

            Vector3d lookingPosition = entity.getLookVec().scale(lookDistance).add(entityPosition);
            BlockRayTraceResult rayTrace = world.rayTraceBlocks(new RayTraceContext(entityPosition, lookingPosition,
                    RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity));

            Direction direction = rayTrace.getType() == RayTraceResult.Type.BLOCK
                    ? rayTrace.getFace().getOpposite()
                    : Direction.getFacingDirections(entity)[0];

            float refHardness = state.getBlockHardness(world, pos);
            ToolType refTool = ItemModularHandheld.getEffectiveTool(state);

//            for (int i = 0; i < pierceAmount; i++) {
//                BlockPos offsetPos = pos.offset(facing, i + 1);
//                BlockState offsetState = world.getBlockState(offsetPos);
//                ToolType effectiveTool = ItemModularHandheld.getEffectiveTool(offsetState);
//
//                int toolLevel = itemStack.getItem().getHarvestLevel(itemStack, effectiveTool, player, offsetState);
//                if (((toolLevel >= 0 && toolLevel >= offsetState.getBlock().getHarvestLevel(offsetState))
//                        || itemStack.canHarvestBlock(offsetState))
//                        && offsetState.getBlockHardness(world, offsetPos) <= refHardness
//                        && refTool != null && ItemModularHandheld.isToolEffective(refTool, offsetState)) {
//                    if (EffectHelper.breakBlock(world, player, itemStack, offsetPos, offsetState, true)) {
//                        continue;
//                    }
//                }
//                break;
//            }
            if (refTool != null && item.getToolLevel(itemStack, refTool) > 0) {
                enqueueBlockBreak(world, player, item, itemStack, direction, pos.offset(direction), refHardness, refTool, pierceAmount);
            }
        }
    }

    private static void enqueueBlockBreak(World world, PlayerEntity player, ModularItem item, ItemStack itemStack, Direction direction, BlockPos pos, float refHardness, ToolType refTool, int remaining) {
        ServerScheduler.schedule(1, () -> {
            BlockState offsetState = world.getBlockState(pos);
            ToolType effectiveTool = ItemModularHandheld.getEffectiveTool(offsetState);

            float blockHardness = offsetState.getBlockHardness(world, pos);
            int toolLevel = itemStack.getItem().getHarvestLevel(itemStack, effectiveTool, player, offsetState);
            if (((toolLevel >= 0 && toolLevel >= offsetState.getBlock().getHarvestLevel(offsetState)) || itemStack.canHarvestBlock(offsetState))
                    && blockHardness != -1
                    && blockHardness <= refHardness
                    && ItemModularHandheld.isToolEffective(refTool, offsetState)) {
                if (EffectHelper.breakBlock(world, player, itemStack, pos, offsetState, true)) {
                    EffectHelper.sendEventToPlayer((ServerPlayerEntity) player, 2001, pos, Block.getStateId(offsetState));

                    CastOptional.cast(item, ItemModularHandheld.class)
                            .ifPresent(itemHandheld -> itemHandheld.applyBreakEffects(itemStack, world, offsetState, pos, player));

                    if (remaining > 0) {
                        enqueueBlockBreak(world, player, item, itemStack, direction, pos.offset(direction), refHardness, refTool, remaining - 1);
                    }
                }
            }
        });
    }
}
