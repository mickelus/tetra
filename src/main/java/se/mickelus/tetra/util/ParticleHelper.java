package se.mickelus.tetra.util;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;
@ParametersAreNonnullByDefault
public class ParticleHelper {
    public static void spawnArmorParticles(ServerLevel world, LivingEntity entity) {
        spawnArmorParticles(world, entity, EquipmentSlot.values()[2 + entity.getRandom().nextInt(4)]);
    }

    public static void spawnArmorParticles(ServerLevel world, LivingEntity entity, EquipmentSlot slot) {
        Random rand = entity.getRandom();
        ItemStack itemStack = entity.getItemBySlot(slot);
        if (!itemStack.isEmpty()) {
            ((ServerLevel) entity.level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, itemStack),
                    entity.getX() + entity.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                    entity.getY() + entity.getBbHeight() * (0.2 + rand.nextGaussian() * 0.4),
                    entity.getZ() + entity.getBbWidth() * (0.3 + rand.nextGaussian() * 0.4),
                    10,
                    0, 0, 0, 0f);
        }
    }
}
