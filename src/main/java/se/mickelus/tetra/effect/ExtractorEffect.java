package se.mickelus.tetra.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;
import se.mickelus.tetra.util.RotationHelper;
import se.mickelus.tetra.util.ToolActionHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class ExtractorEffect {
    public static void breakBlocks(ItemModularHandheld item, ItemStack itemStack, int effectLevel, ServerLevel world, BlockState state, BlockPos pos, LivingEntity entity) {
        Player player = CastOptional.cast(entity, Player.class).orElse(null);

        if (effectLevel > 0) {
            Vec3 entityPosition = entity.getEyePosition(0);
            double lookDistance = Optional.ofNullable(entity.getAttribute(ForgeMod.REACH_DISTANCE.get()))
                    .map(AttributeInstance::getValue)
                    .orElse(5d);

            Vec3 lookingPosition = entity.getLookAngle().scale(lookDistance).add(entityPosition);
            BlockHitResult rayTrace = world.clip(new ClipContext(entityPosition, lookingPosition,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));

            Direction direction = rayTrace.getType() == HitResult.Type.BLOCK
                    ? rayTrace.getDirection().getOpposite()
                    : Direction.orderedByNearest(entity)[0];

            float refHardness = state.getDestroySpeed(world, pos);
            ToolAction refTool = ItemModularHandheld.getEffectiveTool(state);

            double critMultiplier = CritEffect.rollMultiplier(entity.getRandom(), item, itemStack);
            if (critMultiplier != 1) {
                effectLevel *= critMultiplier;
                world.sendParticles(ParticleTypes.ENCHANTED_HIT, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, 15, 0.2D, 0.2D, 0.2D, 0.0D);
            }


            if (refTool != null && item.getToolLevel(itemStack, refTool) > 0) {
                breakRecursive(world, player, item, itemStack, direction, pos, refHardness, refTool, effectLevel);
                item.applyDamage(effectLevel, itemStack, entity);
                item.tickProgression(entity, itemStack, Mth.ceil(effectLevel / 2d));
            }
        }
    }

    private static void breakRecursive(Level world, Player player, ItemModularHandheld item, ItemStack itemStack, Direction direction, BlockPos pos, float refHardness, ToolAction refTool, int remaining) {
        if (remaining > 0) {
            ServerScheduler.schedule(2, () -> breakInner(world, player, item, itemStack, direction, pos, refHardness, refTool));
        }
        if (remaining > 1) {
            ServerScheduler.schedule(4, () -> breakOuter(world, player, item, itemStack, direction, pos, refHardness, refTool));
        }
        if (remaining > 2) {
            ServerScheduler.schedule(6, () -> {
                BlockPos offsetPos = pos.relative(direction);
                if (breakBlock(world, player, item, itemStack, offsetPos, refHardness, refTool)) {
                    breakRecursive(world, player, item, itemStack, direction, offsetPos, refHardness, refTool, remaining - 2);
                }
            });
        }
    }

    private static void breakInner(Level world, Player player, ItemModularHandheld item, ItemStack itemStack, Direction direction, BlockPos pos, float refHardness, ToolAction refTool) {
        Vec3i axis1 = RotationHelper.shiftAxis(direction.getNormal());
        Vec3i axis2 = RotationHelper.shiftAxis(axis1);
        breakBlock(world, player, item, itemStack, pos.offset(axis1), refHardness, refTool);
        breakBlock(world, player, item, itemStack, pos.subtract(axis1), refHardness, refTool);
        breakBlock(world, player, item, itemStack, pos.offset(axis2), refHardness, refTool);
        breakBlock(world, player, item, itemStack, pos.subtract(axis2), refHardness, refTool);
    }

    private static void breakOuter(Level world, Player player, ItemModularHandheld item, ItemStack itemStack, Direction direction, BlockPos pos, float refHardness, ToolAction refTool) {
        Vec3i axis1 = RotationHelper.shiftAxis(direction.getNormal());
        Vec3i axis2 = RotationHelper.shiftAxis(axis1);
        breakBlock(world, player, item, itemStack, pos.offset(axis1).offset(axis2), refHardness, refTool);
        breakBlock(world, player, item, itemStack, pos.subtract(axis1).subtract(axis2), refHardness, refTool);
        breakBlock(world, player, item, itemStack, pos.offset(axis1).subtract(axis2), refHardness, refTool);
        breakBlock(world, player, item, itemStack, pos.subtract(axis1).offset(axis2), refHardness, refTool);
    }


    private static boolean breakBlock(Level world, Player player, ItemModularHandheld item, ItemStack itemStack, BlockPos pos, float refHardness, ToolAction refTool) {
        BlockState offsetState = world.getBlockState(pos);

        float blockHardness = offsetState.getDestroySpeed(world, pos);
        if (ToolActionHelper.playerCanDestroyBlock(player, offsetState, pos, itemStack)
                && blockHardness != -1
                && blockHardness <= refHardness
                && ItemModularHandheld.isToolEffective(refTool, offsetState)) {
            if (EffectHelper.breakBlock(world, player, itemStack, pos, offsetState, true)) {
                EffectHelper.sendEventToPlayer((ServerPlayer) player, 2001, pos, Block.getId(offsetState));

                item.applyBreakEffects(itemStack, world, offsetState, pos, player);

                return true;
            }
        }

        return false;
    }
}
