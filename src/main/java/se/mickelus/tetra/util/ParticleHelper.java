package se.mickelus.tetra.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class ParticleHelper {
    public static void spawnArmorParticles(ServerWorld world, LivingEntity entity) {
        spawnArmorParticles(world, entity, EquipmentSlotType.values()[2 + entity.getRandom().nextInt(4)]);
    }

    public static void spawnArmorParticles(ServerWorld world, LivingEntity entity, EquipmentSlotType slot) {
        Random rand = entity.getRandom();
        ItemStack itemStack = entity.getItemBySlot(slot);
        if (!itemStack.isEmpty()) {
            ((ServerWorld) entity.level).sendParticles(new ItemParticleData(ParticleTypes.ITEM, itemStack),
                    entity.getX() + entity.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                    entity.getY() + entity.getBbHeight() * (0.2 + rand.nextGaussian() * 0.4),
                    entity.getZ() + entity.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                    10,
                    0, 0, 0, 0f);
        }
    }
}
