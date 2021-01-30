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
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.util.CastOptional;
import se.mickelus.tetra.util.RotationHelper;

import java.util.Optional;

public class ExtractorEffect {
    public static void breakBlocks(ModularItem item, ItemStack itemStack, int effectLevel, ServerWorld world, BlockState state, BlockPos pos, LivingEntity entity) {
        PlayerEntity player = CastOptional.cast(entity, PlayerEntity.class).orElse(null);

        if (effectLevel > 0) {
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

            double critMultiplier = CritEffect.rollMultiplier(entity.getRNG(), item, itemStack);
            if (critMultiplier != 1) {
                effectLevel *= critMultiplier;
                world.spawnParticle(ParticleTypes.ENCHANTED_HIT, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, 15, 0.2D, 0.2D, 0.2D, 0.0D);
            }


            if (refTool != null && item.getToolLevel(itemStack, refTool) > 0) {
                breakRecursive(world, player, item, itemStack, direction, pos, refHardness, refTool, effectLevel);
                item.applyDamage(effectLevel, itemStack, entity);
                item.tickProgression(entity, itemStack, MathHelper.ceil(effectLevel / 2d));
            }
        }
    }

    private static void breakRecursive(World world, PlayerEntity player, ModularItem item, ItemStack itemStack, Direction direction, BlockPos pos, float refHardness, ToolType refTool, int remaining) {
        if (remaining > 0) {
            ServerScheduler.schedule(2, () -> breakInner(world, player, item, itemStack, direction, pos, refHardness, refTool));
        }
        if (remaining > 1) {
            ServerScheduler.schedule(4, () -> breakOuter(world, player, item, itemStack, direction, pos, refHardness, refTool));
        }
        if (remaining > 2) {
            ServerScheduler.schedule(6, () -> {
                BlockPos offsetPos = pos.offset(direction);
                if (breakBlock(world, player, item, itemStack, offsetPos, refHardness, refTool)) {
                    breakRecursive(world, player, item, itemStack, direction, offsetPos, refHardness, refTool, remaining - 2);
                }
            });
        }
    }

    private static void breakInner(World world, PlayerEntity player, ModularItem item, ItemStack itemStack, Direction direction, BlockPos pos, float refHardness, ToolType refTool) {
        Vector3i axis1 = RotationHelper.shiftAxis(direction.getDirectionVec());
        Vector3i axis2 = RotationHelper.shiftAxis(axis1);
        breakBlock(world, player, item, itemStack, pos.add(axis1), refHardness, refTool);
        breakBlock(world, player, item, itemStack, pos.subtract(axis1), refHardness, refTool);
        breakBlock(world, player, item, itemStack, pos.add(axis2), refHardness, refTool);
        breakBlock(world, player, item, itemStack, pos.subtract(axis2), refHardness, refTool);
    }

    private static void breakOuter(World world, PlayerEntity player, ModularItem item, ItemStack itemStack, Direction direction, BlockPos pos, float refHardness, ToolType refTool) {
        Vector3i axis1 = RotationHelper.shiftAxis(direction.getDirectionVec());
        Vector3i axis2 = RotationHelper.shiftAxis(axis1);
        breakBlock(world, player, item, itemStack, pos.add(axis1).add(axis2), refHardness, refTool);
        breakBlock(world, player, item, itemStack, pos.subtract(axis1).subtract(axis2), refHardness, refTool);
        breakBlock(world, player, item, itemStack, pos.add(axis1).subtract(axis2), refHardness, refTool);
        breakBlock(world, player, item, itemStack, pos.subtract(axis1).add(axis2), refHardness, refTool);
    }


    private static boolean breakBlock(World world, PlayerEntity player, ModularItem item, ItemStack itemStack, BlockPos pos, float refHardness, ToolType refTool) {
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

                return true;
            }
        }

        return false;
    }
}
