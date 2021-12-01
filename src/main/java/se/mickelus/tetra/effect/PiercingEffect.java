package se.mickelus.tetra.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.common.ToolType;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.util.CastOptional;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
@ParametersAreNonnullByDefault
public class PiercingEffect {
    public static void pierceBlocks(ItemModularHandheld item, ItemStack itemStack, int pierceAmount, ServerLevel world, BlockState state, BlockPos pos, LivingEntity entity) {
        Player player = CastOptional.cast(entity, Player.class).orElse(null);

        if (pierceAmount > 0) {
            double critMultiplier = CritEffect.rollMultiplier(entity.getRandom(), item, itemStack);
            if (critMultiplier != 1) {
                pierceAmount *= critMultiplier;
                world.sendParticles(ParticleTypes.ENCHANTED_HIT, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, 15, 0.2D, 0.2D, 0.2D, 0.0D);
            }

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
                enqueueBlockBreak(world, player, item, itemStack, direction, pos.relative(direction), refHardness, refTool, pierceAmount);
            }
        }
    }

    private static void enqueueBlockBreak(Level world, Player player, ItemModularHandheld item, ItemStack itemStack, Direction direction, BlockPos pos, float refHardness, ToolType refTool, int remaining) {
        ServerScheduler.schedule(1, () -> {
            BlockState offsetState = world.getBlockState(pos);
            ToolType effectiveTool = ItemModularHandheld.getEffectiveTool(offsetState);

            float blockHardness = offsetState.getDestroySpeed(world, pos);
            int toolLevel = itemStack.getItem().getHarvestLevel(itemStack, effectiveTool, player, offsetState);
            if (((toolLevel >= 0 && toolLevel >= offsetState.getBlock().getHarvestLevel(offsetState)) || itemStack.isCorrectToolForDrops(offsetState))
                    && blockHardness != -1
                    && blockHardness <= refHardness
                    && ItemModularHandheld.isToolEffective(refTool, offsetState)) {
                if (EffectHelper.breakBlock(world, player, itemStack, pos, offsetState, true)) {
                    EffectHelper.sendEventToPlayer((ServerPlayer) player, 2001, pos, Block.getId(offsetState));

                    item.applyBreakEffects(itemStack, world, offsetState, pos, player);

                    if (remaining > 0) {
                        enqueueBlockBreak(world, player, item, itemStack, direction, pos.relative(direction), refHardness, refTool, remaining - 1);
                    }
                }
            }
        });
    }
}
