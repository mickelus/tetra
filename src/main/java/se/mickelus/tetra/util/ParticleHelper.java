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
        spawnArmorParticles(world, entity, EquipmentSlotType.values()[2 + entity.getRNG().nextInt(4)]);
    }

    public static void spawnArmorParticles(ServerWorld world, LivingEntity entity, EquipmentSlotType slot) {
        Random rand = entity.getRNG();
        ItemStack itemStack = entity.getItemStackFromSlot(slot);
        if (!itemStack.isEmpty()) {
            ((ServerWorld) entity.world).spawnParticle(new ItemParticleData(ParticleTypes.ITEM, itemStack),
                    entity.getPosX() + entity.getWidth() * (0.3 + rand.nextGaussian() * 0.4),
                    entity.getPosY() + entity.getHeight() * (0.2 + rand.nextGaussian() * 0.4),
                    entity.getPosZ() + entity.getWidth() * (0.3 + rand.nextGaussian() * 0.4),
                    10,
                    0, 0, 0, 0f);
        }
    }
}
