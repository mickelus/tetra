package se.mickelus.tetra.craftingeffect.outcome;

import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
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
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

@ParametersAreNonnullByDefault
public class SpawnEffectCloud implements CraftingEffectOutcome {
    MobEffect effect;
    int amplifier = 0;
    int duration = 600;
    int cloudDuration = 600;
    int waitTime = 10;
    float radius = 3.0f;
    float radiusChange = 0;
    float chance = 1.0f;
    int randomOriginDistance = 0;

    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing,
            Player player, ItemStack[] preMaterials, Map<ToolAction, Integer> tools, Level world,
            UpgradeSchematic schematic, BlockPos pos, BlockState blockState, boolean consumeResources,
            ItemStack[] postMaterials) {

        if (consumeResources && !world.isClientSide() && world.getRandom().nextFloat() < chance) {
            Vec3 spawnPos = Vec3.atBottomCenterOf(pos);
            if (randomOriginDistance > 0) {
                spawnPos = spawnPos.offsetRandom(world.getRandom(), randomOriginDistance);
            }

            AreaEffectCloud cloud = new AreaEffectCloud(EntityType.AREA_EFFECT_CLOUD, world);
            cloud.setOwner(player);
            cloud.setPos(spawnPos.x(), spawnPos.y(), spawnPos.z());
            cloud.setRadius(radius);
            cloud.setDuration(cloudDuration);
            cloud.setWaitTime(waitTime);
            cloud.setRadiusPerTick(radiusChange);

            MobEffectInstance effectInstance = new MobEffectInstance(effect, duration, amplifier, false, true);
            cloud.addEffect(effectInstance);

            world.addFreshEntity(cloud);

            return true;
        }

        return false;
    }
}
