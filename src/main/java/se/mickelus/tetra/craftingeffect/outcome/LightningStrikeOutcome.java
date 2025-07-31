package se.mickelus.tetra.craftingeffect.outcome;

import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.ServerScheduler;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

@ParametersAreNonnullByDefault
public class LightningStrikeOutcome implements CraftingEffectOutcome {
    int randomOriginDistance = 0;
    int delayTicks = 40;
    float chance = 1;

    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing,
            Player player, ItemStack[] preMaterials, Map<ToolAction, Integer> tools, Level world,
            UpgradeSchematic schematic, BlockPos pos, BlockState blockState, boolean consumeResources,
            ItemStack[] postMaterials) {

        if (consumeResources && !world.isClientSide() && world.getRandom().nextFloat() < chance) {
            ServerLevel serverLevel = (ServerLevel) world;
            if (randomOriginDistance > 0) {
                pos = pos.offset(getRandomOffset(world.getRandom(), randomOriginDistance));
            }
            pos = serverLevel.findLightningTargetAround(pos);

            if (delayTicks > 0) {
                BlockPos finalPos = pos;
                RandomSource random = world.getRandom();
                for (int i = 0; i < delayTicks; i += 5) {
                    Vec3 particlePos = Vec3.atBottomCenterOf(pos)
                            .add(random.nextGaussian() - 0.5f, random.nextFloat() * 0.5f, random.nextGaussian() - 0.5f);

                    int randomDelay = random.nextInt(4);
                    ServerScheduler.schedule(i + randomDelay, () ->
                            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, particlePos.x, particlePos.y, particlePos.z, 4, 0, 0, 0, 0));
                }
                ServerScheduler.schedule(delayTicks, () -> spawnLightningBolt(serverLevel, finalPos, (ServerPlayer) player));
            }
            else {
                spawnLightningBolt(serverLevel, pos, (ServerPlayer) player);
            }

            return true;
        }
        return false;
    }

    private static void spawnLightningBolt(ServerLevel serverLevel, BlockPos pos, ServerPlayer causingPlayer) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
        lightning.moveTo(Vec3.atBottomCenterOf(pos));
        lightning.setCause(causingPlayer);
        serverLevel.addFreshEntity(lightning);
    }

    private static BlockPos getRandomOffset(RandomSource random, int randomOriginDistance) {
        return new BlockPos(random.nextInt(randomOriginDistance * 2 + 1) - randomOriginDistance,
                0,
                random.nextInt(randomOriginDistance * 2 + 1) - randomOriginDistance);
    }
}
