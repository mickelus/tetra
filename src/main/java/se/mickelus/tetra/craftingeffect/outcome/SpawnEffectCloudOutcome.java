package se.mickelus.tetra.craftingeffect.outcome;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbility;
import se.mickelus.tetra.effect.EffectHelper;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
public class SpawnEffectCloudOutcome implements CraftingEffectOutcome {
    MobEffect effect;
    int amplifier = 0;
    int duration = 200;
    int cloudDuration = 200;
    int waitTime = 10;
    float radius = 3.0f;
    float radiusChange = 0;
    float chance = 1.0f;
    int randomOriginDistance = 0;

    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] preMaterials, Map<ItemAbility, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos pos, BlockState blockState,
            boolean consumeResources, ItemStack[] postMaterials, float severity) {
        if (consumeResources && !world.isClientSide() && world.getRandom().nextFloat() < chance) {
            Vec3 spawnPos = randomOriginDistance > 0
                    ? Vec3.atBottomCenterOf(findRandomBlockPos(world, pos, randomOriginDistance))
                    : Vec3.atBottomCenterOf(pos);

            AreaEffectCloud cloud = new AreaEffectCloud(EntityType.AREA_EFFECT_CLOUD, world);
            cloud.setOwner(player);
            cloud.setPos(spawnPos.x(), spawnPos.y(), spawnPos.z());
            cloud.setRadius(radius);
            cloud.setDuration(cloudDuration);
            cloud.setWaitTime(waitTime);
            cloud.setRadiusPerTick(radiusChange);

            MobEffectInstance effectInstance = new MobEffectInstance(EffectHelper.effectHolder(effect), duration, amplifier, false, true);
            cloud.addEffect(effectInstance);

            world.addFreshEntity(cloud);

            return true;
        }

        return false;
    }

    private static BlockPos findRandomBlockPos(Level level, BlockPos origin, int radius) {
        BlockPos randomOffset = new BlockPos(level.random.nextIntBetweenInclusive(-radius, radius),
                0,
                level.random.nextIntBetweenInclusive(-radius, radius));

        for (int i = 0; i < 4; i++) {
            BlockPos adjustedPos = randomOffset.below(i).offset(origin);
            if (level.getBlockState(adjustedPos).isAir() && !level.getBlockState(adjustedPos.below()).isAir()) {
                return adjustedPos;
            }
        }
        for (int i = 1; i < 4; i++) {
            BlockPos adjustedPos = randomOffset.above(i).offset(origin);
            if (level.getBlockState(adjustedPos).isAir() && !level.getBlockState(adjustedPos.below()).isAir()) {
                return adjustedPos;
            }
        }
        return randomOffset;
    }
}
