package se.mickelus.tetra.craftingeffect.outcome;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Map;

public class SpawnEntityOutcome implements CraftingEffectOutcome {
    CompoundTag entity;
    int randomOriginDistance = 0;
    float chance = 1.0f;
    ParticleOptions particle;


    @Override
    public boolean apply(ResourceLocation[] unlockedEffects, ItemStack upgradedStack, String slot, boolean isReplacing, Player player,
            ItemStack[] preMaterials, Map<ToolAction, Integer> tools, Level world, UpgradeSchematic schematic, BlockPos pos, BlockState blockState,
            boolean consumeResources, ItemStack[] postMaterials, float severity) {
        if (consumeResources && !world.isClientSide() && world.getRandom().nextFloat() < chance) {
            ServerLevel serverLevel = (ServerLevel) world;
            BlockPos spawnPos = randomOriginDistance > 0
                    ? pos.offset(world.getRandom().nextInt(randomOriginDistance * 2 + 1) - randomOriginDistance,
                    world.getRandom().nextInt(3),
                    world.getRandom().nextInt(randomOriginDistance * 2 + 1) - randomOriginDistance)
                    : pos.above();

            Entity entityInstance = EntityType.loadEntityRecursive(entity, serverLevel, e -> {
                e.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                return e;
            });
            if (entityInstance != null) {
                if (entityInstance instanceof Vex vex) {
                    vex.setBoundOrigin(spawnPos);
                }
                serverLevel.addFreshEntityWithPassengers(entityInstance);
                if (particle != null) {
                    Vec3 particlePosition = entityInstance.position().add(0, entityInstance.getEyeHeight() / 2, 0);
                    serverLevel.sendParticles(particle, particlePosition.x(), particlePosition.y(), particlePosition.z(),
                            20, 0.5, 0.5, 0.5, 0.5);
                }
                return true;
            }
        }
        return false;
    }
}
